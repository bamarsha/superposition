package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.quantum.StateId

/** The primary qubit is used when a gate is applied to an entity without targeting a specific qubit.
  *
  * @param bit the primary qubit
  */
final class PrimaryBit(val bit: StateId[Boolean]) extends Component

/** Contains the component mapper for the primary bit component. */
object PrimaryBit {
  /** The component mapper for the primary bit component. */
  val Mapper: ComponentMapper[PrimaryBit] = ComponentMapper.getFor(classOf[PrimaryBit])
}
