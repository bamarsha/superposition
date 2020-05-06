package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}

final class QuantumObject(val multiverse: Multiverse) extends Component

object QuantumObject {
  val Mapper: ComponentMapper[QuantumObject] = ComponentMapper.getFor(classOf[QuantumObject])
}
