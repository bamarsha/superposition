package superposition

import engine.core.Behavior.{Component, Entity}
import engine.graphics.sprites.Sprite
import engine.util.Color
import engine.util.math.Transformation
import extras.physics.PhysicsComponent

/**
 * The drawable component draws an entity with a sprite at its current position.
 *
 * Note that having a drawable component by itself does nothing; it also needs a [[superposition.UniverseObject]]
 * component to be drawn.
 *
 * @param entity the entity for this component
 * @param sprite the sprite for this entity
 * @param color the color of this entity's sprite
 */
private class Drawable(entity: Entity, var sprite: Sprite, var color: Color) extends Component(entity) {
  private val physics: PhysicsComponent = get(classOf[PhysicsComponent])

  /**
   * Draws this entity.
   */
  def draw(): Unit =
    sprite.draw(Transformation.create(physics.position, 0, 1), color)
}
