package superposition.game.component

import com.badlogic.ashley.core.Component
import superposition.quantum.StateId

final class Quantum(val multiverse: Multiverse, val primary: StateId[Boolean]) extends Component
