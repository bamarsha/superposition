package superposition.graphics

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.gdx.graphics.Color.RED
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line
import com.badlogic.gdx.utils.Disposable
import superposition.component.{Outline, Renderable}
import superposition.entity.Level

/** Renders laser beams.
  *
  * @param level a function that returns the current level
  */
final class OutlineRenderer(level: () => Option[Level]) extends Renderer with Disposable {
  /** The shape renderer. */
  private val shapeRenderer: ShapeRenderer = new ShapeRenderer

  override val family: Family = Family.all(classOf[Outline]).get

  override def render(entity: Entity, deltaTime: Float): Unit = {
    val multiverseView = level().get.multiverseView
    shapeRenderer.setProjectionMatrix(multiverseView.camera.combined)
    val outline = Outline.mapper.get(entity)
    multiverseView.enqueueRenderer(Renderable.mapper.get(entity)) { (universe, _) =>
      if (outline.visible(universe) && multiverseView.isSelected(outline)) {
        shapeRenderer.begin(Line)
        shapeRenderer.setColor(RED)
        shapeRenderer.rect(outline.lowerLeft.x.toFloat, outline.lowerLeft.y.toFloat,
                           outline.size.x.toFloat, outline.size.y.toFloat)
        shapeRenderer.end()
      }
    }
  }

  override def dispose(): Unit = shapeRenderer.dispose()
}
