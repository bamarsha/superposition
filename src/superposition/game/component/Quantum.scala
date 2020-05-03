package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.quantum.StateId

final class Quantum(val multiverse: Multiverse, val primary: StateId[Boolean]) extends Component

object Quantum {
  val Mapper: ComponentMapper[Quantum] = ComponentMapper.getFor(classOf[Quantum])
}
