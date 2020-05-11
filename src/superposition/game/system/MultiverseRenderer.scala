package superposition.game.system

import com.badlogic.ashley.core._
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx.{gl, graphics}
import com.badlogic.gdx.graphics.{Camera, Color, GL20}
import com.badlogic.gdx.graphics.GL20.{GL_BLEND, GL_COLOR_BUFFER_BIT}
import com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888
import com.badlogic.gdx.graphics.g2d.{BitmapFont, SpriteBatch}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.graphics.glutils.{FloatFrameBuffer, FrameBuffer, ShaderProgram, ShapeRenderer}
import superposition.game.ResourceResolver.resolve
import superposition.game.component._
import superposition.graphics.PostProcessingStep

import scala.math.Pi

/** The multiverse renderer. */
final class MultiverseRenderer extends IteratingSystem(Family.all(classOf[Multiverse], classOf[MultiverseView]).get) {
  /** A shape renderer. */
  private val shapeRenderer: ShapeRenderer = new ShapeRenderer

  /** The shader program used to draw each universe. */
  private val shader: ShaderProgram = new ShaderProgram(
    resolve("shaders/universe.vert"),
    resolve("shaders/universe.frag"))


  private val noiseShader: ShaderProgram = new ShaderProgram(
    resolve("shaders/universe.vert"),
    resolve("shaders/totalNoise.frag"))

  /** The sprite batch used to draw each universe. */
  private val batch: SpriteBatch = new SpriteBatch(1000, shader)

  private val batch3: SpriteBatch = new SpriteBatch()

  private val batchN: SpriteBatch = new SpriteBatch(1000, noiseShader)

  /** The frame buffer used to draw each universe. */
  private val buffer: FrameBuffer =
  // TODO: Resize the frame buffer if the window is resized.
    new FrameBuffer(RGBA8888, graphics.getWidth, graphics.getHeight, false)
  private val buffer2: FrameBuffer =
  // TODO: Resize the frame buffer if the window is resized.
    new FrameBuffer(RGBA8888, graphics.getWidth, graphics.getHeight, false)
  private val bufferN: FloatFrameBuffer =
  // TODO: Resize the frame buffer if the window is resized.
    new FloatFrameBuffer(graphics.getWidth, graphics.getHeight, false)

  /** The elapsed time since the system began. */
  private var time: Float = 0

  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    time += deltaTime
    highlightOccupiedCells(entity)
    draw(entity)
    drawState(entity)
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

    buffer2.begin()
    gl.glClearColor(0, 0, 0, 0)
    gl.glClear(GL_COLOR_BUFFER_BIT)
    buffer2.end()

    bufferN.begin()
    gl.glClearColor(0, 0, 0, 0)
    gl.glClear(GL_COLOR_BUFFER_BIT)
    batchN.setProjectionMatrix(multiverseView.camera.combined)
    batchN.begin()
    batchN.enableBlending()
    batchN.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE)
    var i = 0f
    for (universe <- multiverse.universes) {
      noiseShader.setUniformf("time", time + i)
      noiseShader.setUniformf("probability", universe.amplitude.squaredMagnitude.toFloat)
      noiseShader.setUniform4fv("color", Array(1, 1-i/10, i/10, 1), 0, 4)
      i += 10
      batchN.draw(buffer2.getColorBufferTexture, 0, multiverseView.camera.viewportHeight,
                  multiverseView.camera.viewportWidth, -multiverseView.camera.viewportHeight)
      batchN.flush()
    }
    batchN.end()
    bufferN.end()

    i = 0
    for (universe <- multiverse.universes) {
      buffer.begin()
      gl.glClearColor(0, 0, 0, 0)
      gl.glClear(GL_COLOR_BUFFER_BIT)
      multiverseView.drawAll(universe)
      buffer.end()

      drawBuffer(multiverseView.camera, minValue, i, universe.amplitude.squaredMagnitude.toFloat, universe.amplitude.phase.toFloat)
      minValue += universe.amplitude.squaredMagnitude.toFloat
      i += 10
    }

    batch3.setProjectionMatrix(multiverseView.camera.combined)
    batch3.begin()
    batch3.setColor(1, 1, 1, 1)
    batch3.draw(buffer2.getColorBufferTexture, 0, multiverseView.camera.viewportHeight,
                multiverseView.camera.viewportWidth, -multiverseView.camera.viewportHeight)
//    batch3.setColor(1, 1, 1, .91f)
//    batch3.draw(bufferN.getColorBufferTexture, 0, multiverseView.camera.viewportHeight,
//                multiverseView.camera.viewportWidth, -multiverseView.camera.viewportHeight)
    batch3.end()

    multiverseView.emptyDrawingQueue()
  }

  /** The sprite batch. */
  private val batch2: SpriteBatch = new SpriteBatch

  /** The font. */
  private val font2: BitmapFont = new BitmapFont

  def drawState(entity: Entity): Unit = {
    val multiverse = Multiverse.Mapper.get(entity)

    batch2.begin()
    var x = 10f
    for (id <- multiverse.stateIds) {
      var y = 45f
      var maxWidth = 0f

      for (universe <- multiverse.universes) {
        val g = font2.draw(batch2, id.printer(universe.state(id)), x, y)
        maxWidth = math.max(maxWidth, g.width)
        y += 20
      }
      val g = font2.draw(batch2, id.name, x, y)
      maxWidth = math.max(maxWidth, g.width)
      y += 20
      x += maxWidth + 10
    }
    batch2.end()
  }

  /** Draws the contents of the buffer with the shader applied.
    *
    * @param minValue the lower bound of the shader interval assigned to the universe
    * @param maxValue the upper bound of the shader interval assigned to the universe
    * @param phase the phase of the universe
    */
  private def drawBuffer(camera: Camera, minValue: Float, timeOffset: Float, probability: Float, phase: Float): Unit = {
    def drawBatch(action: () => Unit): Unit = {
      batch.begin()
      action()
      batch.draw(buffer.getColorBufferTexture, 0, camera.viewportHeight, camera.viewportWidth, -camera.viewportHeight)
      batch.end()
    }

    buffer2.begin()
    batch.setProjectionMatrix(camera.combined)
    batch.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE, GL20.GL_ONE)
    drawBatch { () =>
      shader.setUniformf("time", time + timeOffset)
      shader.setUniformf("probability", probability)
      shader.setUniformf("hue", minValue)
      shader.setUniform4fv("color", Array(1, 1, 1, 1), 0, 4)

      shader.setUniformi("totalNoise", 1)
      bufferN.getColorBufferTexture.bind(1)
      gl.glActiveTexture(GL20.GL_TEXTURE0)
    }
    batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
    buffer2.end()
  }
}
