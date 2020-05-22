package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.StateId

/** The primary qubit is used when a gate is applied to an entity without targeting a specific qubit.
  *
  * @param bit the primary qubit
  */
final class PrimaryBit(val bit: StateId[Boolean]) extends Component

/** Contains the component mapper for the primary bit component. */
object PrimaryBit {
  /** The component mapper for the primary bit component. */
  val mapper: ComponentMapper[PrimaryBit] = ComponentMapper.getFor(classOf[PrimaryBit])
}
