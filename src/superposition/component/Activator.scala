package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.StateId

/** The activator component can activate objects that require a qubit in a particular state.
  *
  * @param bits the activator qubits
  */
final class Activator(val bits: Seq[StateId[Boolean]]) extends Component

/** Contains the component mapper for the activator component. */
object Activator {
  /** The component mapper for the activator component. */
  val mapper: ComponentMapper[Activator] = ComponentMapper.getFor(classOf[Activator])
}
