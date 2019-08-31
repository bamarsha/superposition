package superposition

import engine.core.Behavior.Component
import engine.graphics.sprites.Sprite
import engine.util.Color
import engine.util.math.Transformation
import extras.physics.PhysicsComponent

/**
 * The drawable component draws an entity with a sprite at its current position.
 *
 * Note that having a drawable component by itself does nothing; it also needs a [[superposition.UniverseObject]]
 * component to be drawn.
 */
private class Drawable extends Component {
  private val physics: PhysicsComponent = require(classOf[PhysicsComponent])

  /**
   * The sprite for this entity.
   */
  var sprite: Sprite = _

  /**
   * The color of this entity's sprite.
   */
  var color: Color = _

  /**
   * Draws this entity.
   */
  def draw(): Unit =
    sprite.draw(Transformation.create(physics.position, 0, 1), color)
}
