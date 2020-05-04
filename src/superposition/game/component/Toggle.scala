package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.quantum.StateId

final class Toggle(val toggle: StateId[Boolean]) extends Component

object Toggle {
  val Mapper: ComponentMapper[Toggle] = ComponentMapper.getFor(classOf[Toggle])
}
