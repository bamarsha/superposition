package superposition.game

import com.badlogic.ashley.core.Component
import superposition.quantum.StateId

/**
 * The player character.
 */
private class Player(var alive: StateId[Boolean], val speed: Float = 6.5f) extends Component
