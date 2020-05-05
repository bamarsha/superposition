package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.{Vector2d, Vector2i}

final class ClassicalPosition(val absolute: Vector2d, val cell: Vector2i) extends Component

object ClassicalPosition {
  val Mapper: ComponentMapper[ClassicalPosition] = ComponentMapper.getFor(classOf[ClassicalPosition])
}
