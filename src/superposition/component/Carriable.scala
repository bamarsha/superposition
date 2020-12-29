package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.StateId

/** Entities with the carriable component can be carried.
  *
  * @param carried the qubit representing the carried state
  */
final class Carriable(val carried: StateId[Boolean]) extends Component

/** Contains the component mapper for the carriable component. */
object Carriable {

  /** The component mapper for the carriable component. */
  val mapper: ComponentMapper[Carriable] = ComponentMapper.getFor(classOf[Carriable])
}
