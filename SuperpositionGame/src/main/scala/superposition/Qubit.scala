package superposition

import engine.core.Behavior
import engine.core.Behavior.Component
import engine.util.Color.{BLACK, WHITE}

/**
 * A qubit is any game object with a binary state.
 */
private class Qubit extends Component {
  Behavior.track(classOf[Qubit])

  /**
   * The universe object for this qubit.
   */
  val universeObject: UniverseObject = using(classOf[UniverseObject])

  private val drawable: Drawable = using(classOf[Drawable])

  private var _on: Boolean = _

  override protected def onCreate(): Unit = {
    drawable.color = if (on) WHITE else BLACK
    universeObject.universe.add(this)
  }

  /**
   * Whether this qubit is in the on or off state.
   */
  def on: Boolean = _on

  def on_=(value: Boolean): Unit = {
    _on = value
    drawable.color = if (value) WHITE else BLACK
  }

  /**
   * Flips this qubit between the on and off states.
   */
  def flip(): Unit = on = !on
}
