package superposition

import engine.core.Behavior
import engine.core.Behavior.Component

/**
 * A qubit is any game object with a binary state.
 */
private class Qubit extends Component {
  Behavior.track(classOf[Qubit])

  /**
   * The universe object for this qubit.
   */
  val universeObject: UniverseObject = require(classOf[UniverseObject])

  /**
   * The index of this qubit.
   */
  var id: Int = _

  /**
   * Whether this qubit is in the on or off state.
   */
  var on: Boolean = _

  override protected def onCreate(): Unit = universeObject.universe.add(this)

  /**
   * Flips this qubit between the on and off states.
   */
  def flip(): Unit = on = !on
}
