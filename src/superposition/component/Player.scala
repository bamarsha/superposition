package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.StateId

/** The player component allows an entity to be controlled by the player.
  *
  * @param alive a qubit representing whether the player is alive
  */
final class Player(val alive: StateId[Boolean]) extends Component

/** Contains the component mapper for the player component. */
object Player {
  /** The component mapper for the player component. */
  val mapper: ComponentMapper[Player] = ComponentMapper.getFor(classOf[Player])
}
