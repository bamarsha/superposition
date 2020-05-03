package superposition.game

import com.badlogic.ashley.core.Component
import superposition.quantum.StateId

private final class Carryable(val carried: StateId[Boolean]) extends Component
