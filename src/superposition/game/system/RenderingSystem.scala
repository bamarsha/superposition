package superposition.game.system

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.ashley.systems.SortedIteratingSystem
import superposition.game.component.Renderable
import superposition.game.system.RenderingSystem.compareLayers
import superposition.graphics.Renderer

/** The rendering system uses the renderers to render all renderable entities.
  *
  * @param renderers the renderers
  */
final class RenderingSystem(renderers: Iterable[Renderer])
  extends SortedIteratingSystem(Family.all(classOf[Renderable]).get, compareLayers) {
  override def processEntity(entity: Entity, deltaTime: Float): Unit =
    for (renderer <- renderers if renderer.family.matches(entity)) {
      renderer.render(entity, deltaTime)
    }
}

/** Functions for the rendering system. */
private object RenderingSystem {
  /** Compares the layers of both entities and returns an integer whose sign indicates the result.
    *
    * @param entity1 the first entity
    * @param entity2 the second entity
    * @return the comparison result
    */
  private def compareLayers(entity1: Entity, entity2: Entity): Int = {
    val layer1 = Renderable.Mapper.get(entity1).layer
    val layer2 = Renderable.Mapper.get(entity2).layer
    layer1.compare(layer2)
  }
}
