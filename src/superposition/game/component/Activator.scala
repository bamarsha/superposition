package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.quantum.StateId

final class Activator(val activator: StateId[Boolean]) extends Component

object Activator {
  val Mapper: ComponentMapper[Activator] = ComponentMapper.getFor(classOf[Activator])
}
