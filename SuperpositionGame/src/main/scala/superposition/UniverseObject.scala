package superposition

import engine.core.Behavior.Component
import extras.physics.PhysicsComponent

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
   * The universe this object belongs to.
   */
  var universe: Universe = _

  /**
   * Copies this object's entity to another universe. Returns the universe object component of the new entity.
   */
  var copyTo: Universe => UniverseObject = _

  /**
   * Called after this object's entity has been copied to a new universe with a map from all objects in the old universe
   * to the corresponding objects in the new universe.
   */
  //noinspection ScalaUnnecessaryParentheses
  var onCopyFinished: Map[UniverseObject, UniverseObject] => Unit = (_ => ())

  override protected def onCreate(): Unit = universe.add(this)
}
