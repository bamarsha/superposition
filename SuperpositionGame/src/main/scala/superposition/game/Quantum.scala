package superposition.game

import com.badlogic.ashley.core.Component
import superposition.quantum.StateId

private class Quantum(val multiverse: Multiverse, val primary: StateId[Boolean]) extends Component
