package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.QExpr.QExpr
import superposition.math._

/** The beam component shoots a laser beam that applies a quantum gate.
  *
  * @param multiverse the multiverse that the beam belongs to
  * @param gate the gate to apply
  * @param direction the direction of the beam
  * @param control the control for the gate
  */
final class Beam(
    multiverse: Multiverse,
    val gate: Gate[StateId[Boolean]],
    val direction: Direction,
    val control: QExpr[BitSeq])
  extends Component {
  /** The most recent target cell, if any. */
  val lastTarget: MetaId[Option[Vector2[Int]]] = multiverse.allocateMeta(None)

  /** The most recent sequence of beams that were fired, if any. */
  val lastBeamSeq: MetaId[Option[BitSeq]] = multiverse.allocateMeta(None)

  /** The amount of time since the beam was most recently activated. */
  val elapsedTime: MetaId[Float] = multiverse.allocateMeta(0f)
}

/** Contains the component mapper for the beam component. */
object Beam {
  /** The component mapper for the beam component. */
  val mapper: ComponentMapper[Beam] = ComponentMapper.getFor(classOf[Beam])
}
