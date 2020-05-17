package superposition.graphics

import com.badlogic.ashley.core._
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.g2d.{Batch, SpriteBatch}
import com.badlogic.gdx.graphics.{Color, GL20}
import superposition.component._
import superposition.game.ResourceResolver.resolve

/** Renders the multiverse. */
final class MultiverseRenderer extends Renderer {
  /** The batch. */
  private val batch: Batch = new SpriteBatch

  private val universeBuffer = new PostProcessedBuffer(resolve("shaders/sprite.frag"))

  private val multiverseBuffer = new PostProcessedBuffer(resolve("shaders/universe.frag"))
  multiverseBuffer.batch.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE, GL20.GL_ONE)

  private val noiseBuffer = new PostProcessedBuffer(resolve("shaders/totalNoise.frag"), true)
  noiseBuffer.batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE)

  /** The elapsed time since the system began. */
  private var time: Float = 0

  override val family: Family = Family.all(classOf[Multiverse], classOf[MultiverseView]).get

  override def render(entity: Entity, deltaTime: Float): Unit = {
    val multiverse = Multiverse.Mapper.get(entity)
    val multiverseView = MultiverseView.Mapper.get(entity)
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
      multiverseView.render(universe, UniverseRenderInfo(new Color(1, 1, 1, .3f).fromHsv(minValue * 360f, 1, 1)))
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
}
