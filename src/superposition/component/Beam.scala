package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math._

/** The beam component shoots a laser beam that applies a quantum gate.
  *
  * @param multiverse the multiverse that the beam belongs to
  * @param gate the gate to apply
  * @param direction the direction of the beam
  * @param controls the cells that must contain a qubit in the |1⟩ state before the beam can be activated
  */
final class Beam(
    multiverse: Multiverse,
    val gate: Gate[StateId[Boolean]],
    val direction: Direction,
    val controls: Universe => Boolean)
  extends Component {
  /** The most recent target cell, if any. */
  val lastTarget: MetaId[Option[Vector2[Int]]] = multiverse.allocateMeta(None)

  /** The amount of time since the beam was most recently activated. */
  val elapsedTime: MetaId[Double] = multiverse.allocateMeta(0)
}

/** Contains the component mapper for the beam component. */
object Beam {
  /** The component mapper for the beam component. */
  val Mapper: ComponentMapper[Beam] = ComponentMapper.getFor(classOf[Beam])
}
