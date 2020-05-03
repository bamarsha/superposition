package superposition.game.component

import com.badlogic.ashley.core.Component
import superposition.quantum.StateId

final class Carryable(val carried: StateId[Boolean]) extends Component
