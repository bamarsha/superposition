package superposition.graphics

import com.badlogic.ashley.core._
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.GL20.GL_BLEND
import com.badlogic.gdx.graphics.g2d.{Batch, SpriteBatch}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.{Filled, Line}
import com.badlogic.gdx.graphics.{Color, GL20}
import com.badlogic.gdx.utils.Disposable
import superposition.component._
import superposition.game.ResourceResolver.resolve
import superposition.math.Universe

/** Renders the multiverse. */
final class MultiverseRenderer extends Renderer with Disposable {
  /** The batch. */
  private val batch: Batch = new SpriteBatch

  /** The shape renderer. */
  private val shapeRenderer: ShapeRenderer = new ShapeRenderer

  /** The universe frame buffer. */
  private val universeBuffer = new PostProcessedBuffer(resolve("shaders/sprite.frag"))

  /** The multiverse frame buffer. */
  private val multiverseBuffer = new PostProcessedBuffer(resolve("shaders/universe.frag"))
  multiverseBuffer.batch.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE, GL20.GL_ONE)

  /** The noise frame buffer. */
  private val noiseBuffer = new PostProcessedBuffer(resolve("shaders/totalNoise.frag"), true)
  noiseBuffer.batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE)

  /** The elapsed time since the system began. */
  private var time: Float = 0

  override val family: Family = Family.all(classOf[Multiverse], classOf[MultiverseView]).get

  override def render(entity: Entity, deltaTime: Float): Unit = {
    val multiverse = Multiverse.mapper.get(entity)
    val multiverseView = MultiverseView.mapper.get(entity)
    time += deltaTime

    noiseBuffer.clear()
    noiseBuffer.capture(multiverseView.camera) { () =>
      var timeOffset = 0f
      for (universe <- multiverse.universes) {
        noiseBuffer.shader.setUniformf("time", time + timeOffset)
        noiseBuffer.shader.setUniformf("probability", universe.amplitude.squaredMagnitude.toFloat)
        multiverseBuffer.draw(noiseBuffer.batch, multiverseView.camera)
        noiseBuffer.batch.flush()
        timeOffset += 10
      }
    }

    multiverseBuffer.clear()

    var minValue = 0f
    var timeOffset = 0
    for (universe <- multiverse.universes) {
      universeBuffer.clear()
      universeBuffer.buffer.begin()
      val color = new Color(1, 1, 1, .3f).fromHsv(minValue * 360f, 1, 1)
      multiverseView.render(universe, UniverseRenderInfo(color))
      drawCompass(multiverseView, universe, color)
      universeBuffer.buffer.end()

      val probability = universe.amplitude.squaredMagnitude.toFloat
      multiverseBuffer.capture(multiverseView.camera) { () =>
        multiverseBuffer.shader.setUniformf("time", time + timeOffset)
        multiverseBuffer.shader.setUniformf("probability", probability)
        multiverseBuffer.shader.setUniformi("totalNoise", 1)
        noiseBuffer.buffer.getColorBufferTexture.bind(1)
        gl.glActiveTexture(GL20.GL_TEXTURE0)
        universeBuffer.draw(multiverseBuffer.batch, multiverseView.camera)
      }

      minValue += probability
      timeOffset += 10
    }

    batch.setProjectionMatrix(multiverseView.camera.combined)
    batch.begin()
    multiverseBuffer.draw(batch, multiverseView.camera)
    batch.end()

    multiverseView.clearRenderers()
  }

  def drawCompass(multiverseView: MultiverseView, universe: Universe, color: Color): Unit = {
    shapeRenderer.setProjectionMatrix(multiverseView.camera.combined)
    val radius = .4f

    shapeRenderer.begin(Filled)
    gl.glEnable(GL_BLEND)
    shapeRenderer.setColor(.5f, .5f, .5f, .5f)
    shapeRenderer.circle(1.5f, 1.5f, radius, 24)
    shapeRenderer.end()

    shapeRenderer.begin(Line)
    gl.glDisable(GL_BLEND)
    shapeRenderer.setColor(0, 0, 0, 1)
    shapeRenderer.circle(1.5f, 1.5f, radius, 24)
    shapeRenderer.setColor(color)
    shapeRenderer.rectLine(1.5f, 1.5f,
                           1.5f + radius * math.cos(universe.amplitude.phase).toFloat,
                           1.5f + radius * math.sin(universe.amplitude.phase).toFloat,
                           .001f)
    shapeRenderer.end()
  }

  override def dispose(): Unit = {
    batch.dispose()
    universeBuffer.dispose()
    multiverseBuffer.dispose()
    noiseBuffer.dispose()
  }
}
