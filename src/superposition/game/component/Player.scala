package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.quantum.StateId

/**
 * The player character.
 */
final class Player(var alive: StateId[Boolean], val speed: Float = 6.5f) extends Component

object Player {
  val Mapper: ComponentMapper[Player] = ComponentMapper.getFor(classOf[Player])
}
