package superposition.game

import com.badlogic.ashley.core._
import com.badlogic.gdx.Gdx.{gl, graphics}
import com.badlogic.gdx.graphics.GL20.GL_BLEND
import com.badlogic.gdx.graphics.g2d.{BitmapFont, GlyphLayout, SpriteBatch}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.graphics.{Color, GL20}
import com.badlogic.gdx.math.Matrix4
import superposition.game.component._
import superposition.graphics.PostProcessingStep

/** Renders the multiverse. */
private final class MultiverseRenderer extends Renderer {
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

  override val family: Family = Family.all(classOf[Multiverse], classOf[MultiverseView]).get

  override def render(entity: Entity, deltaTime: Float): Unit = {
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
  private def draw(entity: Entity): Unit = {
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
      multiverseView.render(universe, UniverseRenderParams((new Color).fromHsv(minValue * 360f, 1, 1)))
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
    multiverseStep.drawTo(batch)
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
