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
 * When an entity with the universe object component is created, it is automatically added to its universe's list of
 * objects.
 *
 * @param entity the entity for this component
 * @param id the ID of this object
 * @param universe the universe this object belongs to
 * @param copyTo copies this object's entity to another universe and returns the new entity
 */
private class UniverseObject(entity: Entity,
                             val id: UniversalId,
                             val universe: Universe,
                             val copyTo: Universe => Entity) extends Component(entity) {
  /**
   * The physics component of this object.
   */
  val physics: PhysicsComponent = getComponent(classOf[PhysicsComponent])

  /**
   * The drawable component of this object.
   */
  val drawable: Drawable = getComponent(classOf[Drawable])

  override protected def onCreate(): Unit = universe.add(this)
}
