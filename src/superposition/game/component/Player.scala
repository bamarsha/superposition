package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.quantum.StateId

/**
 * The player character.
 */
final class Player(val multiverse: Multiverse, var alive: StateId[Boolean]) extends Component

object Player {
  val Mapper: ComponentMapper[Player] = ComponentMapper.getFor(classOf[Player])

  val Speed: Float = 6.5f
}
