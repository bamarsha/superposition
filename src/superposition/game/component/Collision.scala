package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.Vector2i
import superposition.quantum.Universe

import scala.Function.const

final class Collision(val cells: Universe => Set[Vector2i] = const(Set.empty)) extends Component

object Collision {
  val Mapper: ComponentMapper[Collision] = ComponentMapper.getFor(classOf[Collision])
}