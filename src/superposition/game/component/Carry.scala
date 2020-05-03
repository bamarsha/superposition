package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.quantum.StateId

final class Carry(val carried: StateId[Boolean]) extends Component

object Carry {
  val Mapper: ComponentMapper[Carry] = ComponentMapper.getFor(classOf[Carry])
}
