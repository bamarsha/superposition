package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import com.badlogic.gdx.Gdx.{gl, graphics, input}
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT
import com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.{FrameBuffer, ShaderProgram}
import com.badlogic.gdx.math.Vector3
import superposition.game.ResourceResolver.resolve
import superposition.math.Vector2
import superposition.quantum.{MetaId, Universe}

import scala.math.Pi

/** The multiverse view component manages the camera and provides rendering effects for the multiverse.
  *
  * @param multiverse the multiverse corresponding to this view
  * @param camera the camera used to view the multiverse
  */
final class MultiverseView(multiverse: Multiverse, val camera: Camera) extends Component {
  /** The shader program used to draw each universe. */
  private val shader: ShaderProgram = new ShaderProgram(
    resolve("shaders/universe.vert"),
    resolve("shaders/universe.frag"))

  /** The interval assigned to each universe that is used by the shader. */
  private val shaderInterval: MetaId[(Float, Float)] = multiverse.allocateMeta((0, 0))

  /** The sprite batch used to draw each universe. */
  private val batch: SpriteBatch = new SpriteBatch(1000, shader)

  /** The frame buffer used to draw each universe. */
  private val buffer: FrameBuffer =
  // TODO: Resize the frame buffer if the window is resized.
    new FrameBuffer(RGBA8888, graphics.getWidth, graphics.getHeight, false)

  /** The elapsed time since the multiverse began. */
  private var time: Float = 0

  /** Returns true if the cell is selected by the mouse.
    *
    * @param cell the cell to check
    * @return true if the cell is selected by the mouse
    */
  def isSelected(cell: Vector2[Int]): Boolean = {
    val mouse = camera.unproject(new Vector3(input.getX, input.getY, 0))
    cell == Vector2(mouse.x.floor.toInt, mouse.y.floor.toInt)
  }

  /** Updates the view of the multiverse each frame.
    *
    * @param deltaTime the time elapsed since the last update
    */
  def update(deltaTime: Float): Unit = {
    time += deltaTime
    var minValue = 0f
    multiverse.updateMetaWith(shaderInterval) { _ => universe =>
      val maxValue = minValue + universe.amplitude.squaredMagnitude.toFloat
      val interval = (minValue, maxValue)
      minValue = maxValue
      interval
    }
  }

  /** Performs a drawing action for each universe and applies rendering effects to it.
    *
    * @param action the drawing action
    */
  def draw(action: Universe => Unit): Unit =
    for (universe <- multiverse.universes) {
      buffer.begin()
      gl.glClearColor(0, 0, 0, 0)
      gl.glClear(GL_COLOR_BUFFER_BIT)
      action(universe)
      buffer.end()
      val (minValue, maxValue) = universe.meta(shaderInterval)
      drawBuffer(minValue, maxValue, universe.amplitude.phase.toFloat)
    }

  /** Draws the contents of the buffer with the shader applied.
    *
    * @param minValue the lower bound of the shader interval assigned to the universe
    * @param maxValue the upper bound of the shader interval assigned to the universe
    * @param phase the phase of the universe
    */
  private def drawBuffer(minValue: Float, maxValue: Float, phase: Float): Unit = {
    def drawBatch(action: () => Unit): Unit = {
      batch.begin()
      action()
      batch.draw(buffer.getColorBufferTexture, 0, camera.viewportHeight, camera.viewportWidth, -camera.viewportHeight)
      batch.end()
    }

    batch.setProjectionMatrix(camera.combined)
    drawBatch { () =>
      shader.setUniformf("time", time)
      shader.setUniformf("minVal", minValue)
      shader.setUniformf("maxVal", maxValue)
      shader.setUniformf("hue", (phase / (2 * Pi)).toFloat)
      shader.setUniform4fv("color", Array(1, 1, 1, 1), 0, 4)
    }
    drawBatch { () =>
      shader.setUniformf("minVal", 0f)
      shader.setUniformf("maxVal", 1f)
      shader.setUniform4fv("color", Array(1, 1, 1, 0.1f), 0, 4)
    }
  }
}

/** Contains the component mapper for the multiverse view component. */
object MultiverseView {
  /** The component mapper for the multiverse view component. */
  val Mapper: ComponentMapper[MultiverseView] = ComponentMapper.getFor(classOf[MultiverseView])
}
