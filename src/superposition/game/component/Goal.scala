package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.Vector2
import superposition.quantum.StateId

/** The goal component can require that another entity must be in the same cell as the goal.
  *
  * @param needsThunk a thunk that returns the position qudit of the entity that needs to reach the goal
  */
final class Goal(needsThunk: () => StateId[Vector2[Int]]) extends Component {
  /** The position qudit of the entity that needs to reach the goal. */
  lazy val needs: StateId[Vector2[Int]] = needsThunk()
}

/** Contains the component mapper for the goal component. */
object Goal {
  /** The component mapper for the goal component. */
  val Mapper: ComponentMapper[Goal] = ComponentMapper.getFor(classOf[Goal])
}
