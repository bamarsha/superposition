package superposition.game

import engine.core.Behavior.{Component, Entity}
import engine.core.Game.track
import superposition.math.Vec2i
import superposition.quantum.{StateId, Universe}

import scala.Function.const
import scala.jdk.CollectionConverters._

class UniverseComponent(entity: Entity,
                        val position: Option[StateId[Vec2i]] = None,
                        val primaryBit: Option[StateId[Boolean]] = None,
                        val blockingCells: Universe => Set[Vec2i] = const(Set.empty))
  extends Component(entity)

object UniverseComponent {
  val All: Iterable[UniverseComponent] = track(classOf[UniverseComponent]).asScala
}
