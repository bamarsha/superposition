package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.{MetaId, StateId, Vector2}

/** The quantum position component has a position that may depend on which universe the entity is in.
  *
  * @param absolute the qudit representing the absolute position in camera coordinates
  * @param cell the qudit representing the cell position in grid coordinates
  * @param relative the position relative to the current cell
  */
final class QuantumPosition(
    val absolute: MetaId[Vector2[Double]],
    val cell: StateId[Vector2[Int]],
    var relative: Vector2[Double],
    val blockedByGrates: Boolean = false)
  extends Component

/** Contains the component mapper for the quantum position component. */
object QuantumPosition {
  /** The component mapper for the quantum position component. */
  val mapper: ComponentMapper[QuantumPosition] = ComponentMapper.getFor(classOf[QuantumPosition])
}
