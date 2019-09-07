package superposition

import engine.core.Behavior.{Component, Entity}

/**
 * The bit component holds a binary state for a universe object.
 *
 * @param entity   the entity for this component
 * @param _on      the initial state of this bit
 * @param onChange called when the state of this bit changes
 */
private final class Bit(entity: Entity,
                        private var _on: Boolean = false,
                        onChange: Boolean => Unit = _ => ()) extends Component(entity) {
  /**
   * The universe object for this bit.
   */
  lazy val universeObject: UniverseObject = get(classOf[UniverseObject])

  /**
   * Whether this bit is in the on or off state.
   */
  def on: Boolean = _on

  def on_=(value: Boolean): Unit =
    if (value != on) {
      _on = value
      onChange(value)
    }
}
