package superposition

import java.util

import engine.core.Behavior.Entity
import engine.core.Game.dt
import engine.core.{Behavior, Game}
import engine.graphics.Camera
import engine.graphics.Camera.Camera2d
import engine.graphics.Graphics.drawRectangle
import engine.graphics.opengl.{Framebuffer, Shader, Texture}
import engine.util.Color
import engine.util.Color.CLEAR
import engine.util.math.{Transformation, Vec2d, Vec4d}
import extras.physics.Rectangle
import extras.tiles.{Tilemap, TilemapRenderer}

import scala.jdk.CollectionConverters._
import scala.math.{Pi, sqrt}

/**
 * Contains settings and initialization for the multiverse.
 */
private object Multiverse {
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
  def declareSubsystem[T <: Behavior](cls: Class[T], f: (Multiverse, ObjectId, Iterable[T]) => Unit): Unit = {
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
 * @param universe the initial universe
 * @param tiles    the tiles in the multiverse
 */
private final class Multiverse(universe: => Universe, tiles: Tilemap) extends Entity {

  import Multiverse._

  /**
   * The walls in the multiverse.
   */
  val walls: Set[Cell] =
    (for (layer <- tiles.layers.asScala
          if layer.properties.asScala.get("Collision").exists(_.value.toBoolean);
          x <- 0 until layer.width;
          y <- 0 until layer.height
          if layer.data.tiles(x)(y) != 0) yield {
      Cell(
        (y + layer.offsetY.toDouble / tiles.tileHeight).round.toInt,
        (x + layer.offsetX.toDouble / tiles.tileWidth).round.toInt
      )
    }).toSet

  /**
   * The bounding box of the multiverse's tile map.
   */
  val boundingBox: Rectangle = new Rectangle(new Vec2d(0, 0), new Vec2d(tiles.width, tiles.height))

  private var _universes: Option[List[Universe]] = None

  private var frameBuffer: Framebuffer = _
  private var colorBuffer: Texture = _
  private var time: Double = 0

  private val tileRenderer: TilemapRenderer =
    new TilemapRenderer(tiles, source => Texture.load(getClass.getResource(source)))

  /**
   * The universes in the multiverse.
   */
  def universes: List[Universe] =
    _universes match {
      case Some(us) => us
      case None =>
        _universes = Some(List(universe))
        _universes.get
    }

  //noinspection ScalaUnusedSymbol
  private def universes_=(value: List[Universe]): Unit =
    _universes = Some(value)

  override protected def onCreate(): Unit = {
    universes.foreach(Game.create)
    frameBuffer = new Framebuffer()
    colorBuffer = frameBuffer.attachColorBuffer()
  }

  override protected def onDestroy(): Unit = universes.foreach(Game.destroy)

  /**
   * Returns true if it is valid to apply the quantum gate to the target object with the controls.
   *
   * @param gate     the gate to apply
   * @param target   the target qubit
   * @param controls the controls
   * @return true if it is valid to apply the quantum gate to the target object with the controls
   */
  def canApplyGate(gate: Gate.Value, target: ObjectId, controls: Control*): Boolean = {
    val controlled = withControls(controls: _*)
    gate match {
      case Gate.Up => controlled.forall(u => u.cellOpen(u.objects(target).cell.up))
      case Gate.Down => controlled.forall(u => u.cellOpen(u.objects(target).cell.down))
      case Gate.Left => controlled.forall(u => u.cellOpen(u.objects(target).cell.left))
      case Gate.Right => controlled.forall(u => u.cellOpen(u.objects(target).cell.right))
      case _ => true
    }
  }

  /**
   * Applies the quantum gate to the target object with optional controls.
   *
   * @param gate     the gate to apply
   * @param target   the target object
   * @param key      the target key in the object's bit map, or None to use the default key
   * @param controls the controls
   */
  def applyGate(gate: Gate.Value, target: ObjectId, key: Option[String], controls: Control*): Unit = {
    require(canApplyGate(gate, target, controls: _*), "Invalid gate")
    require(
      controls.forall {
        case BitControl(id, _) if id == target => !Gate.logicGate(gate)
        case PositionControl(id, _) if id == target => !Gate.positionGate(gate)
        case _ => true
      },
      "Controlling using the same ID and type as the target"
    )

    for (u <- withControls(controls: _*)) {
      val k = key.getOrElse(u.bitMaps(target).defaultKey)
      gate match {
        case Gate.X => u.bitMaps(target).state += k -> !u.bitMaps(target).state(k)
        case Gate.Z =>
          if (u.bitMaps(target).state(k)) {
            u.amplitude *= Complex(-1)
          }
        case Gate.T =>
          if (u.bitMaps(target).state(k)) {
            u.amplitude *= Complex.polar(1, Pi / 4)
          }
        case Gate.H =>
          u.amplitude /= Complex(sqrt(2))
          val copy = u.copy()
          if (isCreated) {
            Game.create(copy)
          }
          if (u.bitMaps(target).state(k)) {
            u.amplitude *= Complex(-1)
          }
          copy.bitMaps(target).state += k -> !copy.bitMaps(target).state(k)
          universes = copy :: universes
        case Gate.Up => u.objects(target).cell = u.objects(target).cell.up
        case Gate.Down => u.objects(target).cell = u.objects(target).cell.down
        case Gate.Left => u.objects(target).cell = u.objects(target).cell.left
        case Gate.Right => u.objects(target).cell = u.objects(target).cell.right
      }
    }
    if (gate == Gate.H) {
      combine()
    }
  }

  private def step(): Unit = {
    normalize()
    draw()
  }

  private def withControls(controls: Control*): List[Universe] =
    universes.filter(u => controls.forall {
      case BitControl(id, key -> state) => u.bitMaps(id).state.get(key).contains(state)
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
    tileRenderer.draw(Transformation.IDENTITY, Color.WHITE)

    val activeCells = universes.flatMap(_.bitMaps.values)
      .withFilter(bitMap => bitMap.state.contains("alive") || bitMap.state.contains("carried"))
      .map(_.obj.cell)
      .toSet
    for (cell <- activeCells) {
      drawRectangle(Transformation.create(cell.toVec2d, 0, 1), new Color(1, 1, 1, 0.3))
    }

    time += dt
    UniverseShader.setUniform("time", time.asInstanceOf[Float])
    var minValue = 0.0
    for (u <- universes) {
      val maxValue = minValue + u.amplitude.squaredMagnitude

      frameBuffer.clear(CLEAR)
      u.objects.values.map(_.entity).toSeq.sortBy(_.layer).foreach(_.draw())

      val camera = new Camera2d()
      camera.lowerLeft = new Vec2d(-1, -1)
      Camera.current = camera

      UniverseShader.setMVP(Transformation.IDENTITY)
      UniverseShader.setUniform("minVal", minValue.toFloat)
      UniverseShader.setUniform("maxVal", maxValue.toFloat)
      UniverseShader.setUniform("hue", (u.amplitude.phase / (2 * Pi)).toFloat)
      UniverseShader.setUniform("color", new Vec4d(1, 1, 1, 1))
      Framebuffer.drawToWindow(colorBuffer, UniverseShader)

      UniverseShader.setUniform("minVal", 0f)
      UniverseShader.setUniform("maxVal", 1f)
      UniverseShader.setUniform("color", new Vec4d(1, 1, 1, 0.1))
      Framebuffer.drawToWindow(colorBuffer, UniverseShader)

      Camera.current = Camera.camera2d
      minValue = maxValue
    }
  }
}
