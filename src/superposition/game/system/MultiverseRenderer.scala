package superposition.game.system

import com.badlogic.ashley.core._
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx.{gl, graphics}
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20.{GL_BLEND, GL_COLOR_BUFFER_BIT}
import com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.graphics.glutils.{FrameBuffer, ShaderProgram, ShapeRenderer}
import superposition.game.ResourceResolver.resolve
import superposition.game.component._

import scala.math.Pi

/** The multiverse renderer. */
final class MultiverseRenderer extends IteratingSystem(Family.all(classOf[Multiverse], classOf[MultiverseView]).get) {
  /** A shape renderer. */
  private val shapeRenderer: ShapeRenderer = new ShapeRenderer

  /** The shader program used to draw each universe. */
  private val shader: ShaderProgram = new ShaderProgram(
    resolve("shaders/universe.vert"),
    resolve("shaders/universe.frag"))

  /** The sprite batch used to draw each universe. */
  private val batch: SpriteBatch = new SpriteBatch(1000, shader)

  /** The frame buffer used to draw each universe. */
  private val buffer: FrameBuffer =
  // TODO: Resize the frame buffer if the window is resized.
    new FrameBuffer(RGBA8888, graphics.getWidth, graphics.getHeight, false)

  /** The elapsed time since the system began. */
  private var time: Float = 0

  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    time += deltaTime
    highlightOccupiedCells(entity)
    draw(entity)
  }

  /** Highlights all cells occupied by an entity in the multiverse.
    *
    * @param entity the multiverse entity
    */
  private def highlightOccupiedCells(entity: Entity): Unit = {
    val multiverse = Multiverse.Mapper.get(entity)
    val occupiedCells =
      (for {
        entity <- multiverse.entities if QuantumPosition.Mapper.has(entity)
        position = QuantumPosition.Mapper.get(entity)
        universe <- multiverse.universes
      } yield universe.state(position.cell)).toSet

    gl.glEnable(GL_BLEND)
    shapeRenderer.setProjectionMatrix(MultiverseView.Mapper.get(entity).camera.combined)
    shapeRenderer.begin(ShapeType.Filled)
    shapeRenderer.setColor(1, 1, 1, 0.3f)
    for (cell <- occupiedCells) {
      shapeRenderer.rect(cell.x, cell.y, 1, 1)
    }
    shapeRenderer.end()
    gl.glDisable(GL_BLEND)
  }

  /** Draws the multiverse.
    *
    * @param entity the multiverse entity
    */
  def draw(entity: Entity): Unit = {
    val multiverse = Multiverse.Mapper.get(entity)
    val multiverseView = MultiverseView.Mapper.get(entity)
    var minValue = 0f
    for (universe <- multiverse.universes) {
      buffer.begin()
      gl.glClearColor(0, 0, 0, 0)
      gl.glClear(GL_COLOR_BUFFER_BIT)
      multiverseView.drawAll(universe)
      buffer.end()

      val maxValue = minValue + universe.amplitude.squaredMagnitude.toFloat
      drawBuffer(multiverseView.camera, minValue, maxValue, universe.amplitude.phase.toFloat)
      minValue = maxValue
    }
    multiverseView.emptyDrawingQueue()
  }

  /** Draws the contents of the buffer with the shader applied.
    *
    * @param minValue the lower bound of the shader interval assigned to the universe
    * @param maxValue the upper bound of the shader interval assigned to the universe
    * @param phase the phase of the universe
    */
  private def drawBuffer(camera: Camera, minValue: Float, maxValue: Float, phase: Float): Unit = {
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
      shader.setUniform4fv("color", Array(1, 1, 1, 0.2f), 0, 4)
    }
  }
}
