package superposition.graphics

import com.badlogic.ashley.core._
import com.badlogic.gdx.Gdx.{gl, graphics}
import com.badlogic.gdx.graphics.GL20.GL_BLEND
import com.badlogic.gdx.graphics.g2d.{BitmapFont, GlyphLayout, SpriteBatch}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.graphics.{Color, GL20}
import com.badlogic.gdx.math.Matrix4
import superposition.component._
import superposition.game.ResourceResolver.resolve

/** Renders the multiverse. */
final class MultiverseRenderer extends Renderer {
  /** A shape renderer. */
  private val shapeRenderer: ShapeRenderer = new ShapeRenderer

  /** The sprite batch. */
  private val batch: SpriteBatch = new SpriteBatch

  /** The font. */
  private val font: BitmapFont = new BitmapFont

  private val universeBuffer = new PostProcessedBuffer(resolve("shaders/sprite.frag"))

  private val multiverseBuffer = new PostProcessedBuffer(resolve("shaders/universe.frag"))
  multiverseBuffer.batch.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE, GL20.GL_ONE)

  private val noiseBuffer = new PostProcessedBuffer(resolve("shaders/totalNoise.frag"), true)
  noiseBuffer.batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE)

  /** The elapsed time since the system began. */
  private var time: Float = 0

  override val family: Family = Family.all(classOf[Multiverse], classOf[MultiverseView]).get

  override def render(entity: Entity, deltaTime: Float): Unit = {
    time += deltaTime
    draw(entity)
    drawState(entity)
  }

  /** Draws the multiverse.
    *
    * @param entity the multiverse entity
    */
  private def draw(entity: Entity): Unit = {
    val multiverse = Multiverse.Mapper.get(entity)
    val multiverseView = MultiverseView.Mapper.get(entity)

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

  private def drawState(entity: Entity): Unit = {
    val multiverse = Multiverse.Mapper.get(entity)

    val header = multiverse.stateIds.map(_.name)
    val allPrints = multiverse.universes.map(multiverse.printUniverse).transpose
    val important = allPrints.map(l => l.exists(_ != l.head))
    val widths = allPrints.zip(header).map(l => (l._1 :+ l._2).map { s =>
    val layout = new GlyphLayout()
      layout.setText(font, s)
      layout.width + 10f
    }.max)
    val xs = widths.scanLeft(16f)(_ + _)
    val ys = multiverse.universes.map(_ => -20f).scanLeft(graphics.getHeight - 16f)(_ + _)
    val minValues = multiverse.universes.map(_.amplitude.squaredMagnitude.toFloat).scanLeft(0f)(_ + _)

    gl.glEnable(GL_BLEND)
    shapeRenderer.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, graphics.getWidth(), graphics.getHeight()))
    shapeRenderer.begin(ShapeType.Filled)
    for ((y, minValue) <- ys.zip(None +: minValues.map(Some(_)))) {
      val color = new Color().fromHsv(minValue.map(_ * 360f).getOrElse(0f), if (minValue.isDefined) 1 else 0, 1)
      color.a = .5f
      shapeRenderer.setColor(color)
      shapeRenderer.rect(12f, y + 2, widths.sum - 2, -16)
    }
    shapeRenderer.end()

    batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, graphics.getWidth(), graphics.getHeight()))
    batch.begin()
    for ((((h, toPrint), i), x) <- header.zip(allPrints).zip(important).zip(xs)) {
      font.setColor(if (i) Color.WHITE else new Color(0x9f9f9fff))
      for ((y, p) <- ys.zip(h +: toPrint)) {
        font.draw(batch, p, x, y)
      }
    }
    batch.end()
  }
}
