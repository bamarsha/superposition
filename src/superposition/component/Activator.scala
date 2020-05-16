package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.StateId

/** The activator component can activate objects that require a qubit in a particular state.
  *
  * @param activator the activator qubit
  */
final class Activator(val activator: StateId[Boolean]) extends Component

/** Contains the component mapper for the activator component. */
object Activator {
  /** The component mapper for the activator component. */
  val Mapper: ComponentMapper[Activator] = ComponentMapper.getFor(classOf[Activator])
}
