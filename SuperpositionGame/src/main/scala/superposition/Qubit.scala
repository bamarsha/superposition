package superposition

import engine.core.Behavior.{Component, Entity}

/**
 * A qubit is any game object with a binary state.
 *
 * @param entity the entity for this component
 * @param onChange called when the state of this qubit changes
 */
private class Qubit(entity: Entity, onChange: Boolean => Unit = _ => ()) extends Component(entity) {
  /**
   * The universe object for this qubit.
   */
  lazy val universeObject: UniverseObject = get(classOf[UniverseObject])

  private var _on: Boolean = _

  /**
   * Whether this qubit is in the on or off state.
   */
  def on: Boolean = _on

  def on_=(value: Boolean): Unit =
    if (value != on) {
      _on = value
      onChange(value)
    }

  /**
   * Flips this qubit between the on and off states.
   */
  def flip(): Unit = on = !on
}
