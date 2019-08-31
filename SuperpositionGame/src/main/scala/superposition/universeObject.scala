package superposition

import engine.core.Behavior.{Component, Entity}
import extras.physics.PhysicsComponent

/**
 * Represents the ID of a universe object.
 *
 * @param value the ID
 */
private case class UniversalId(value: Int) extends AnyVal

/**
 * A universe object is any object that exists within a particular universe.
 *
 * When an entity with the universe object component is created, it is automatically added to its universe's list of
 * objects.
 */
private class UniverseObject extends Component {
  /**
   * The physics component of this object.
   */
  val physics: PhysicsComponent = require(classOf[PhysicsComponent])

  /**
   * The drawable component of this object.
   */
  val drawable: Drawable = require(classOf[Drawable])

  /**
   * The ID of this object.
   *
   * It is unique within a universe but not within the multiverse; copies of this component's entity in other universes
   * will have the same ID.
   */
  var id: UniversalId = _

  /**
   * The universe this object belongs to.
   */
  var universe: Universe = _

  /**
   * Copies this object's entity to another universe. Returns the new entity.
   */
  var copyTo: Universe => Entity = _

  override protected def onCreate(): Unit = universe.add(this)
}
