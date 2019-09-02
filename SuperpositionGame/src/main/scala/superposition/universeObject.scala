package superposition

import engine.core.Behavior.{Component, Entity}
import extras.physics.PhysicsComponent

/**
 * Represents the ID of a universe object.
 *
 * A universal ID is unique within a universe but not within the multiverse; copies of an entity in other universes will
 * have the same ID.
 *
 * @param value the ID
 */
private case class UniversalId(value: Int) extends AnyVal

/**
 * A universe object is any object that exists within a particular universe.
 *
 * @param entity the entity for this component
 * @param universe the universe this object belongs to
 * @param id the ID of this object
 */
private class UniverseObject(entity: Entity with Copyable[_ <: Entity],
                             var universe: Universe,
                             val id: UniversalId) extends Component(entity) {
  /**
   * The physics component of this object.
   */
  val physics: PhysicsComponent = get(classOf[PhysicsComponent])

  /**
   * The drawable component of this object.
   */
  val drawable: Drawable = get(classOf[Drawable])
}
