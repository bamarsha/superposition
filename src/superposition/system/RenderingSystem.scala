package superposition.system

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.ashley.systems.IteratingSystem
import superposition.component.Renderable
import superposition.graphics.Renderer

/** The rendering system uses the renderers to render all renderable entities.
  *
  * @param renderers the renderers
  */
final class RenderingSystem(renderers: Iterable[Renderer])
  extends IteratingSystem(Family.all(classOf[Renderable]).get) {
  override def processEntity(entity: Entity, deltaTime: Float): Unit =
    for (renderer <- renderers if renderer.family.matches(entity)) {
      renderer.render(entity, deltaTime)
    }
}
