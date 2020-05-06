package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.Vector2i
import superposition.quantum.Universe

import scala.Function.const

final class Collider(val cells: Universe => Set[Vector2i] = const(Set.empty)) extends Component

object Collider {
  val Mapper: ComponentMapper[Collider] = ComponentMapper.getFor(classOf[Collider])
}
