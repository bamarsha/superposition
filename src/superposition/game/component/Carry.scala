package superposition.game.component

import com.badlogic.ashley.core.Component
import superposition.quantum.StateId

final class Carry(val carried: StateId[Boolean]) extends Component
