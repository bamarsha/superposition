package superposition.game

import com.badlogic.ashley.core.Component
import superposition.math.{Direction, Vector2i}
import superposition.quantum.{Gate, MetaId, StateId}

private final class Beam(
  multiverse: Multiverse,
  val gate: Gate[StateId[Boolean]],
  val source: Vector2i,
  val direction: Direction,
  val controls: Iterable[Vector2i])
  extends Component {

  val lastTarget: MetaId[Option[Vector2i]] = multiverse.allocateMeta(None)

  val elapsedTime: MetaId[Double] = multiverse.allocateMeta(0)

  val path: LazyList[Vector2i] = LazyList.iterate(source)(_ + direction.toVec2i).tail
}

private object Beam {
  val Length: Int = 25
}
