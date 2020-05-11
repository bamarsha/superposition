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

  /** The sprite batch. */
  private val batch: SpriteBatch = new SpriteBatch

  /** The font. */
  private val font: BitmapFont = new BitmapFont

  private val universeStep = new PostProcessingStep()

  private val multiverseStep = new PostProcessingStep("universe")
  multiverseStep.batch.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE, GL20.GL_ONE)

  private val noiseStep = new PostProcessingStep("totalNoise", true)
  noiseStep.batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE)


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

    universeStep.camera = multiverseView.camera
    multiverseStep.camera = multiverseView.camera
    noiseStep.camera = multiverseView.camera

    noiseStep.clear()
    noiseStep.run {
      var timeOffset = 0f
      for (universe <- multiverse.universes) {
        noiseStep.shader.setUniformf("time", time + timeOffset)
        noiseStep.shader.setUniformf("probability", universe.amplitude.squaredMagnitude.toFloat)
        multiverseStep.drawTo(noiseStep.batch)
        noiseStep.batch.flush()
        timeOffset += 10
      }
    }

    multiverseStep.clear()

    var minValue = 0f
    var timeOffset = 0
    for (universe <- multiverse.universes) {
      universeStep.clear()
      universeStep.buffer.begin()
      multiverseView.drawAll(universe)
      universeStep.buffer.end()

      val probability = universe.amplitude.squaredMagnitude.toFloat
      multiverseStep.run {
        multiverseStep.shader.setUniformf("time", time + timeOffset)
        multiverseStep.shader.setUniformf("probability", probability)
        multiverseStep.shader.setUniformf("hue", minValue)
        multiverseStep.shader.setUniformi("totalNoise", 1)
        noiseStep.buffer.getColorBufferTexture.bind(1)
        gl.glActiveTexture(GL20.GL_TEXTURE0)
        universeStep.drawTo(multiverseStep.batch)
      }

      minValue += probability
      timeOffset += 10
    }

    batch.setProjectionMatrix(multiverseView.camera.combined)
    batch.begin()
    batch.setColor(1, 1, 1, 1)
    multiverseStep.drawTo(batch)
    batch.end()

    multiverseView.emptyDrawingQueue()
  }

  def drawState(entity: Entity): Unit = {
    val multiverse = Multiverse.Mapper.get(entity)

    batch.begin()
    var x = 10f
    for (id <- multiverse.stateIds) {
      var y = 45f
      var maxWidth = 0f

      for (universe <- multiverse.universes) {
        val g = font.draw(batch, id.printer(universe.state(id)), x, y)
        maxWidth = math.max(maxWidth, g.width)
        y += 20
      }
      val g = font.draw(batch, id.name, x, y)
      maxWidth = math.max(maxWidth, g.width)
      y += 20
      x += maxWidth + 10
    }
    batch.end()
  }
}
