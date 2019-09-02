package superposition

import engine.core.Behavior.Entity
import engine.core.Game.dt
import engine.core.{Game, Input}
import engine.graphics.Camera
import engine.graphics.Camera.Camera2d
import engine.graphics.opengl.{Framebuffer, Shader, Texture}
import engine.util.Color
import engine.util.Color.CLEAR
import engine.util.math.{Transformation, Vec2d}
import extras.tiles.{Tilemap, TilemapRenderer}
import org.lwjgl.glfw.GLFW._

import scala.math.{Pi, sqrt}

/**
 * Contains settings and initialization for the multiverse.
 */
private object Multiverse {

  private object Gate extends Enumeration {
    val X, Z, T, H = Value
  }

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
}

/**
 * The multiverse is a collection of universes.
 *
 * Multiple universes represent qubits in superposition. The multiverse can apply quantum gates to qubits by changing
 * the amplitude of a universe or creating a copy of a universe.
 *
 * @param _universes the initial universes in the multiverse
 * @param walls      the list of walls in the multiverse
 */
private final class Multiverse(_universes: => List[Universe], val walls: List[Wall]) extends Entity {

  import Multiverse._

  private var universes: List[Universe] = _
  private var frameBuffer: Framebuffer = _
  private var colorBuffer: Texture = _
  private var time: Double = 0

  private var tiles: Tilemap = Tilemap.load(getClass.getResource("level1.tmx"))
  private var tileRenderer: TilemapRenderer = new TilemapRenderer(tiles, source => Texture.load(getClass.getResource(source)))

  override protected def onCreate(): Unit = {
    universes = _universes
    universes.foreach(Game.create(_))
    frameBuffer = new Framebuffer()
    colorBuffer = frameBuffer.attachColorBuffer()
  }

  private def step(): Unit = {
    val selected = universes
      .flatMap(_.qubits.values)
      .filter(_.universeObject.position.value.sub(Input.mouse()).length() < 0.5)
      .map(_.universeObject.id)
      .toSet
    for ((key, gate) <- GateKeys) {
      if (Input.keyJustPressed(key)) {
        selected.foreach(applyGate(gate, _))
      }
    }
    combine()
    normalize()
    draw()
  }

  private def applyGate(gate: Gate.Value, target: UniversalId, controls: UniversalId*): Unit = {
    for (u <- universes.filter(u => controls.forall(u.qubits(_).on))) {
      gate match {
        case Gate.X => u.qubits(target).flip()
        case Gate.Z =>
          if (u.qubits(target).on) {
            u.amplitude *= Complex(-1)
          }
        case Gate.T =>
          if (u.qubits(target).on) {
            u.amplitude *= Complex.polar(1, Pi / 4)
          }
        case Gate.H =>
          u.amplitude /= Complex(sqrt(2))
          val copy = u.copy()
          Game.create(copy)
          if (u.qubits(target).on) {
            u.amplitude *= Complex(-1)
          }
          copy.qubits(target).flip()
          universes = copy :: universes
      }
    }
  }

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

    walls.foreach(_.draw())

    var minValue = 0.0
    for (u <- universes) {
      val maxValue = minValue + u.amplitude.squaredMagnitude

      frameBuffer.clear(CLEAR)
      u.objects.values.foreach(_.drawable.draw())

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
