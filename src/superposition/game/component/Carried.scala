package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.quantum.StateId

final class Carried(val carried: StateId[Boolean]) extends Component

object Carried {
  val Mapper: ComponentMapper[Carried] = ComponentMapper.getFor(classOf[Carried])
}
