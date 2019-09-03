package superposition

import engine.core.Behavior.{Component, Entity}
import engine.util.math.Vec2d
import extras.physics.{PositionComponent, Rectangle}

/**
 * A universe object is any object that exists within a particular universe.
 *
 * @param entity              the entity for this component
 * @param universe            the universe this object belongs to
 * @param id                  the ID of this object
 * @param hitboxSize          the size of this object's hitbox
 * @param collidesWithObjects whether this object collides with other objects in the universe (excluding walls)
 */
private final class UniverseObject(entity: Entity with Copyable[_ <: Entity],
                                   var universe: Universe,
                                   val id: UniversalId,
                                   val hitboxSize: Vec2d = new Vec2d(0, 0),
                                   var collidesWithObjects: Boolean = false) extends Component(entity) {
  /**
   * The position component of this object.
   */
  lazy val position: PositionComponent = get(classOf[PositionComponent])

  /**
   * The drawable component of this object.
   */
  lazy val drawable: Drawable = get(classOf[Drawable])

  /**
   * Returns true if this object would collide with any other object or wall at the position.
   *
   * @param position the position to test for collision
   * @return true if this object would collide with any other object or wall at the position
   */
  def collides(position: Vec2d): Boolean = {
    val hitbox = Rectangle.fromCenterSize(position, hitboxSize)
    val otherObjects = universe.objects.values
      .filter(o => o.entity != (this: Component[_]).entity && o.collidesWithObjects)
      .map(o => Rectangle.fromCenterSize(o.position.value, o.hitboxSize))
    universe.walls.appendedAll(otherObjects).exists(hitbox.intersects)
  }
}
