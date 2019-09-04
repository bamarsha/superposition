package superposition

import engine.core.Behavior.{Component, Entity}
import engine.core.Game

/**
 * Contains initialization for the draw component.
 */
private object Draw {
  /**
   * Declares the draw system.
   */
  def declareSystem(): Unit =
    Game.declareSystem(classOf[Draw], (_: Draw).draw())
}

/**
 * The draw component automatically draws an entity.
 *
 * @param entity the entity for this component
 */
private final class Draw(entity: Entity) extends Component(entity) {
  private val drawable: Drawable = get(classOf[Drawable])

  private def draw(): Unit = drawable.draw()
}
