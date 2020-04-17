package superposition.game

import engine.core.Behavior.{Component, Entity}
import engine.core.Game.track
import superposition.math.Vec2i
import superposition.quantum.{StateId, Universe}

import scala.jdk.CollectionConverters._

object UniverseComponent {
  val All: Iterable[UniverseComponent] = track(classOf[UniverseComponent]).asScala
}

class UniverseComponent(entity: Entity, multiverse: Multiverse) extends Component(entity) {

  var position: Option[StateId[Vec2i]] = None
  var primaryBit: Option[StateId[Boolean]] = None

  var blockingCells: Universe => List[Vec2i] = _ => List()

}
