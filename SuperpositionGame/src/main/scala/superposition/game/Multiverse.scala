package superposition.game

import engine.core.Behavior.Entity
import engine.core.Game
import engine.core.Game.dt
import engine.graphics.Camera
import engine.graphics.Camera.Camera2d
import engine.graphics.Graphics.drawRectangle
import engine.graphics.opengl.{Framebuffer, Shader, Texture}
import engine.util.Color
import engine.util.Color.CLEAR
import engine.util.math.{Transformation, Vec2d, Vec4d}
import extras.physics.Rectangle
import extras.tiles.{Tilemap, TilemapRenderer}
import superposition.types.math.{Cell, Complex}
import superposition.types.quantum.{Gate, Id, Universe}

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
private final class Multiverse(universe: Universe, tiles: Tilemap) extends Entity {

  import Multiverse._

  /**
   * The bounding box of the multiverse's tile map.
   */
  val boundingBox: Rectangle = new Rectangle(new Vec2d(0, 0), new Vec2d(tiles.width, tiles.height))


  private var frameBuffer: Framebuffer = _
  private var colorBuffer: Texture = _
  private var time: Double = 0

  private val tileRenderer: TilemapRenderer =
    new TilemapRenderer(tiles, source => Texture.load(getClass.getResource(source)))

  var universes: List[Universe] = List(universe)

  override protected def onCreate(): Unit = {
    frameBuffer = new Framebuffer()
    colorBuffer = frameBuffer.attachColorBuffer()
  }

  /**
   * Applies the quantum gate to the target object with optional controls.
   *
   * @param gate     the gate to apply
   * @param t        the target object
   */
  def applyGate[T](gate: Gate[T], t: T): Boolean = {
    val newUniverses = Gate.applyToAll(t)(gate)(universes)
    val success = newUniverses.forall(_.isValid)
    if (success) {
      universes = newUniverses
      combine()
    }
    success
  }

  def createId[T](t: T): Id[T] = {
    val i = new Id[T] {}
    universes = universes.map(_.set(i)(t))
    i
  }

  def createIdMeta[T](t: T): Id[T] = {
    val i = new Id[T] {}
    universes = universes.map(_.setMeta(i)(t))
    i
  }

  private def step(): Unit = {
    normalize()
    draw()
  }

  private def combine(): Unit = {
    universes = universes
      .groupMapReduce(_.state)(identity)((u1, u2) => u1 + u2.amplitude)
      .values
      .filter(_.amplitude.squaredMagnitude > 1e-6)
      .toList
    normalize()
  }

  private def normalize(): Unit = {
    val sum = universes.map(_.amplitude.squaredMagnitude).sum
    universes = universes.map(_ / Complex(sqrt(sum)))
  }

  private def draw(): Unit = {
    tileRenderer.draw(Transformation.IDENTITY, Color.WHITE)

    universes.flatMap(u =>UniverseComponent.All.filter(_.position.isDefined).map(uc => u.get(uc.position.get)))
      .toSet.foreach((cell: Cell) =>
        drawRectangle(Transformation.create(cell.toVec2d, 0, 1), new Color(1, 1, 1, 0.3)))

    time += dt
    UniverseShader.setUniform("time", time.asInstanceOf[Float])
    var minValue = 0.0
    for (u <- universes) {
      val maxValue = minValue + u.amplitude.squaredMagnitude

      frameBuffer.clear(CLEAR)
//      u.objects.values.map(_.entity).toSeq.sortBy(_.layer).foreach(_.draw())
      SpriteComponent.All.toList.sortBy(_.layer).foreach(_.draw(u))

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
