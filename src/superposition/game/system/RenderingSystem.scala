package superposition.game.system

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.ashley.systems.SortedIteratingSystem
import superposition.game.component.Renderable
import superposition.game.system.RenderingSystem.{RenderingAction, compareLayers}

/** The rendering system uses the collection of renderers to render all renderable entities.
  *
  * @param renderers the collection of rendering actions and their corresponding component families
  */
final class RenderingSystem(renderers: Iterable[(Family, RenderingAction)])
  extends SortedIteratingSystem(Family.all(classOf[Renderable]).get, compareLayers) {
  override def processEntity(entity: Entity, deltaTime: Float): Unit =
    for ((family, render) <- renderers if family.matches(entity)) {
      render(entity)
    }
}

/** Types and functions for the rendering system. */
object RenderingSystem {
  /** A rendering action is a function that accepts an entity and renders it to the screen. */
  type RenderingAction = Entity => Unit

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
