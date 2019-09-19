package superposition

import engine.core.Behavior.{Component, Entity}

import scala.Function.const

/**
 * Contains boolean states for universe objects.
 *
 * @param entity     the entity for this component
 * @param _state     the initial state of the bits
 * @param defaultKey the default key to use in the state map when no specific key is requested
 * @param onChange   called when the state of this bit map changes
 */
private final class BitMap(entity: Entity,
                           private var _state: Map[String, Boolean],
                           val defaultKey: String,
                           onChange: Map[String, Boolean] => Unit = const(())) extends Component(entity) {
  require(_state.contains(defaultKey), "Map does not contain default key")

  /**
   * The universe object for this bit map.
   */
  lazy val obj: UniverseObject = get(classOf[UniverseObject])

  /**
   * The state of this bit map.
   */
  def state: Map[String, Boolean] = _state

  def state_=(value: Map[String, Boolean]): Unit = {
    require(state.keySet == value.keySet, "Map key set changed")
    _state = value
    onChange(value)
  }
}
