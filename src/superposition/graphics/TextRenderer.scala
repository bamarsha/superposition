package superposition.graphics

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20.GL_BLEND
import com.badlogic.gdx.graphics.g2d.{Batch, BitmapFont, GlyphLayout, SpriteBatch}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Disposable
import superposition.component._
import superposition.entity.Level
import superposition.math.Universe

/** Renders sprites. */
final class TextRenderer(level: () => Option[Level]) extends Renderer with Disposable {
  /** The shape renderer. */
  private val shapeRenderer: ShapeRenderer = new ShapeRenderer

  /** The batch. */
  private val batch: Batch = new SpriteBatch

  /** The display font. */
  private val font: BitmapFont = new BitmapFont

  /** A glyph layout. */
  private val glyphLayout: GlyphLayout = new GlyphLayout

  override val family: Family = Family.all(classOf[Text]).get

  override def render(entity: Entity, deltaTime: Float): Unit = {
    val multiverseView = level().get.multiverseView
    val dependentState = Renderable.mapper.get(entity).dependentState

    multiverseView.enqueueRenderer(dependentState) { (universe, renderInfo) =>
      draw(entity, multiverseView.camera.combined, universe, renderInfo)
    }
  }

  /** Draws the entity's text.
    *
    * @param entity the entity
    * @param universe the universe to draw in
    * @param renderInfo the rendering information for the universe
    */
  private def draw(entity: Entity, mat: Matrix4, universe: Universe, renderInfo: UniverseRenderInfo): Unit = {
    val text = Text.mapper.get(entity)
    glyphLayout.setText(font, text.text)

    val scale = 1 / 96f
    val border = 4
    val x = text.pos.x.toFloat / scale - glyphLayout.width / 2
    val y = text.pos.y.toFloat / scale + glyphLayout.height / 2
    val mat2 = new Matrix4(mat).scale(scale, scale, 1)

    shapeRenderer.setProjectionMatrix(mat2)
    shapeRenderer.begin(Filled)
    shapeRenderer.setColor(0, 0, 0, 0.75f)
    gl.glEnable(GL_BLEND)
    shapeRenderer.rect(x - border, y + border + 1, glyphLayout.width + 2 * border, -glyphLayout.height - 2 * border)
    shapeRenderer.end()

    batch.setProjectionMatrix(mat2)
    batch.begin()
    font.setColor(Color.WHITE)
    font.draw(batch, text.text, x, y)
    batch.end()
  }

  override def dispose(): Unit = {
    shapeRenderer.dispose()
    batch.dispose()
  }
}
