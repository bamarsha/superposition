package superposition

import engine.core.Behavior.{Component, Entity}
import engine.graphics.sprites.Sprite
import engine.util.Color
import engine.util.Color.WHITE
import engine.util.math.{Transformation, Vec2d}
import extras.physics.PositionComponent

/**
 * Adds a sprite to an entity.
 * <p>
 * Note that having this component by itself does nothing; it also needs a [[superposition.UniverseObject]] component to
 * be drawn.
 *
 * @param entity the entity for this component
 * @param sprite the sprite for this entity
 * @param scale  the scale of the sprite
 * @param color  the color of the sprite
 */
private final class SpriteComponent(entity: Entity,
                                    var sprite: Sprite,
                                    var scale: Vec2d = new Vec2d(1, 1),
                                    var color: Color = WHITE) extends Component(entity) {
  private val position: PositionComponent = get(classOf[PositionComponent])

  /**
   * Draws this sprite.
   */
  def draw(): Unit =
    sprite.draw(Transformation.create(position.value, 0, scale), color)
}
