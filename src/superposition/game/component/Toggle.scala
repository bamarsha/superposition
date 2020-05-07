package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.quantum.StateId

/** The toggle component allows some state of an entity to be toggled on and off.
  *
  * @param toggle the toggleable qubit
  */
final class Toggle(val toggle: StateId[Boolean]) extends Component

/** Contains the component mapper for the toggle component. */
object Toggle {
  /** The component mapper for the toggle component. */
  val Mapper: ComponentMapper[Toggle] = ComponentMapper.getFor(classOf[Toggle])
}
