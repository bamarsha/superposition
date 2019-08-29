package superposition

import engine.core.Behavior
import engine.core.Behavior.Component
import extras.physics.PhysicsComponent

/**
 * A universe object is any object that exists within a particular universe.
 */
private class UniverseObject extends Component {
  Behavior.track(classOf[UniverseObject])

  /**
   * The physics component of this game object.
   */
  val physics: PhysicsComponent = require(classOf[PhysicsComponent])

  /**
   * The universe this game object belongs to.
   */
  var universe: Universe = _

  /**
   * Copies this game object to another universe.
   */
  var copyTo: Universe => Unit = _

  /**
   * Draws this game object.
   */
  var draw: () => Unit = _

  override protected def onCreate(): Unit = universe.add(this)
}
