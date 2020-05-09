package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.Vector2

/** The classical position component has a position that does not depend on which universe the entity is in.
  *
  * @param absolute the absolute position in camera coordinates
  * @param cell the cell position in grid coordinates
  */
final class ClassicalPosition(val absolute: Vector2[Double], val cell: Vector2[Int]) extends Component

/** Contains the component mapper for the classical position component. */
object ClassicalPosition {
  /** The component mapper for the classical position component. */
  val Mapper: ComponentMapper[ClassicalPosition] = ComponentMapper.getFor(classOf[ClassicalPosition])
}
