package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.StateId

/** Entities with the carried component can be carried.
  *
  * @param carried the qubit representing the carried state
  */
final class Carried(val carried: StateId[Boolean]) extends Component

/** Contains the component mapper for the carried component. */
object Carried {
  /** The component mapper for the carried component. */
  val Mapper: ComponentMapper[Carried] = ComponentMapper.getFor(classOf[Carried])
}
