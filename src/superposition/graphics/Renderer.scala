package superposition.graphics

import com.badlogic.ashley.core.{Entity, Family}

/** Renders entities. */
trait Renderer {
  /** The component family that each rendered entity must have. */
  def family: Family

  /** Renders the entity.
    *
    * The entity has the [[superposition.game.component.Renderable]] component and matches the component family for this
    * renderer.
    *
    * @param entity the entity
    * @param deltaTime the time elapsed since the last frame
    */
  def render(entity: Entity, deltaTime: Float): Unit
}
