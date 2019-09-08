package superposition

import java.util

import engine.core.Behavior.Entity
import engine.core.Game.dt
import engine.core.{Behavior, Game, Input}
import engine.graphics.Camera
import engine.graphics.Camera.Camera2d
import engine.graphics.opengl.{Framebuffer, Shader, Texture}
import engine.util.Color
import engine.util.Color.CLEAR
import engine.util.math.{Transformation, Vec2d}
import extras.tiles.{Tilemap, TilemapRenderer}
import org.lwjgl.glfw.GLFW._

import scala.jdk.CollectionConverters._
import scala.math.{Pi, sqrt}

/**
 * Contains settings and initialization for the multiverse.
 */
private object Multiverse {

  private val GateKeys: List[(Int, Gate.Value)] = List(
    (GLFW_KEY_X, Gate.X),
    (GLFW_KEY_Z, Gate.Z),
    (GLFW_KEY_T, Gate.T),
    (GLFW_KEY_H, Gate.H)
  )

  private val UniverseShader: Shader = Shader.load(classOf[Multiverse].getResource(_), "shaders/universe")

  /**
   * Declares the multiverse system.
   */
  def declareSystem(): Unit =
    Game.declareSystem(classOf[Multiverse], (_: Multiverse).step())

  /**
   * Declares a subsystem of the multiverse.
   * <p>
   * Subsystems group behaviors by their universal ID, so all copies of a behavior can be processed at the same time.
   *
   * @param cls the behavior class
   * @param f   the system
   * @tparam T the type of the behavior
   */
  def declareSubsystem[T <: Behavior](cls: Class[T], f: (Multiverse, UniversalId, Iterable[T]) => Unit): Unit = {
    Game.declareGroupSystem(cls, (behaviors: util.Collection[T]) => behaviors.asScala
      .groupBy(b => {
        val u = b.get(classOf[UniverseObject])
        (u.multiverse, u.id)
      })
      .foreachEntry { case ((multiverse, id), behaviors) => f(multiverse, id, behaviors) }
    )
  }
}

/**
 * The multiverse is a collection of universes.
 *
 * Multiple universes represent qubits in superposition. The multiverse can apply quantum gates to qubits by changing
 * the amplitude of a universe or creating a copy of a universe.
 *
 * @param _universes the initial universes in the multiverse
 * @param tiles      the tiles in the multiverse
 */
private final class Multiverse(_universes: => List[Universe], tiles: Tilemap) extends Entity {

  import Multiverse._

  /**
   * The walls in the multiverse.
   */
  val walls: Set[Cell] =
    (for (layer <- tiles.layers.asScala
          if layer.properties.asScala.exists(p => p.name == "collision" && p.value.toBoolean);
          x <- 0 until layer.width;
          y <- 0 until layer.height
          if layer.data.tiles(x)(y) != 0) yield {
      Cell(
        (y - 9 + layer.offsetY.toDouble / tiles.tileHeight).round,
        (x - 16 + layer.offsetX.toDouble / tiles.tileWidth).round
      )
    }).toSet

  private var universes: List[Universe] = _

  private var frameBuffer: Framebuffer = _
  private var colorBuffer: Texture = _
  private var time: Double = 0

  private val tileRenderer: TilemapRenderer =
    new TilemapRenderer(tiles, source => Texture.load(getClass.getResource(source)))

  override protected def onCreate(): Unit = {
    universes = _universes
    universes.foreach(Game.create(_))
    frameBuffer = new Framebuffer()
    colorBuffer = frameBuffer.attachColorBuffer()
  }

  /**
   * Returns the bits that are in the cell in all universes.
   *
   * @param cell the cell to find bits in
   * @return the bits that are in the cell in all universes
   */
  def bitsInCell(cell: Cell): Set[UniversalId] =
    universes.flatMap(_.bitsInCell(cell)).toSet

  /**
   * Returns true if it is valid to apply the quantum gate to the target qubit with the controls.
   *
   * @param gate     the gate to apply
   * @param target   the target qubit
   * @param controls the controls
   * @return true if it is valid to apply the quantum gate to the target qubit with the controls
   */
  def canApplyGate(gate: Gate.Value, target: UniversalId, controls: Control*): Boolean = {
    val controlled = withControls(controls: _*)
    gate match {
      case Gate.Up => controlled.forall(u => cellOpen(u.objects(target).cell.up))
      case Gate.Down => controlled.forall(u => cellOpen(u.objects(target).cell.down))
      case Gate.Left => controlled.forall(u => cellOpen(u.objects(target).cell.left))
      case Gate.Right => controlled.forall(u => cellOpen(u.objects(target).cell.right))
      case _ => true
    }
  }

  /**
   * Applies the quantum gate to the target qubit with optional controls.
   *
   * @param gate     the gate to apply
   * @param target   the target qubit
   * @param controls the controls
   */
  def applyGate(gate: Gate.Value, target: UniversalId, controls: Control*): Unit = {
    require(canApplyGate(gate, target, controls: _*), "Applying this gate is not valid")
    require(
      controls.forall({
        case BitControl(id, _) if id == target => !Gate.logicGate(gate)
        case PositionControl(id, _) if id == target => !Gate.positionGate(gate)
        case _ => true
      }),
      "Controlling using the same ID and type as the target"
    )

    for (u <- withControls(controls: _*)) {
      gate match {
        case Gate.X => u.bits(target).on = !u.bits(target).on
        case Gate.Z =>
          if (u.bits(target).on) {
            u.amplitude *= Complex(-1)
          }
        case Gate.T =>
          if (u.bits(target).on) {
            u.amplitude *= Complex.polar(1, Pi / 4)
          }
        case Gate.H =>
          u.amplitude /= Complex(sqrt(2))
          val copy = u.copy()
          Game.create(copy)
          if (u.bits(target).on) {
            u.amplitude *= Complex(-1)
          }
          copy.bits(target).on = !copy.bits(target).on
          universes = copy :: universes
        case Gate.Up => u.objects(target).cell = u.objects(target).cell.up
        case Gate.Down => u.objects(target).cell = u.objects(target).cell.down
        case Gate.Left => u.objects(target).cell = u.objects(target).cell.left
        case Gate.Right => u.objects(target).cell = u.objects(target).cell.right
      }
    }
  }

  private def cellOpen(cell: Cell): Boolean =
    !walls.contains(cell) &&
      // TODO: This will prevent some valid moves if a cell is open for some copies of the player.
      universes.forall(u => u.objects.values.filter(_.collision).forall(_.cell != cell))

  private def step(): Unit = {
    val cell = Cell(Input.mouse().y.floor.toLong, Input.mouse().x.floor.toLong)
    val selected = bitsInCell(cell)
    for ((key, gate) <- GateKeys) {
      if (Input.keyJustPressed(key)) {
        selected.foreach(id => applyGate(gate, id, PositionControl(id, cell)))
      }
    }
    combine()
    normalize()
    draw()
  }

  private def withControls(controls: Control*): List[Universe] =
    universes.filter(u => controls.forall {
      case BitControl(id, on) => u.bits(id).on == on
      case PositionControl(id, cell) => u.objects(id).cell == cell
    })

  private def combine(): Unit = {
    val (combined, removed) = universes
      .groupMapReduce(_.state)(identity)((u1, u2) => {
        u2.amplitude += u1.amplitude
        Game.destroy(u1)
        u2
      })
      .values
      .partition(_.amplitude.squaredMagnitude > 1e-6)
    removed.foreach(Game.destroy)
    universes = combined.toList
  }

  private def normalize(): Unit = {
    val sum = universes.map(_.amplitude.squaredMagnitude).sum
    for (u <- universes) {
      u.amplitude /= Complex(sqrt(sum))
    }
  }

  private def draw(): Unit = {
    tileRenderer.draw(Transformation.create(new Vec2d(-16, -9), 0, 1), Color.WHITE)

    time += dt()
    UniverseShader.setUniform("time", time.asInstanceOf[Float])

    var minValue = 0.0
    for (u <- universes) {
      val maxValue = minValue + u.amplitude.squaredMagnitude

      frameBuffer.clear(CLEAR)
      u.objects.values.foreach(_.entity.draw())

      val camera = new Camera2d()
      camera.lowerLeft = new Vec2d(-1, -1)
      Camera.current = camera
      UniverseShader.setMVP(Transformation.IDENTITY)
      UniverseShader.setUniform("minVal", minValue.asInstanceOf[Float])
      UniverseShader.setUniform("maxVal", maxValue.asInstanceOf[Float])
      UniverseShader.setUniform("hue", (u.amplitude.phase / (2 * Pi)).asInstanceOf[Float])
      Framebuffer.drawToWindow(colorBuffer, UniverseShader)
      Camera.current = Camera.camera2d

      minValue = maxValue
    }
  }
}
