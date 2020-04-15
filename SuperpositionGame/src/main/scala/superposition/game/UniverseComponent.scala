package superposition.game

import engine.core.Behavior.{Component, Entity}
import superposition.math.Cell
import superposition.quantum.{Id, Universe}
import engine.core.Game.track
import scala.jdk.CollectionConverters._

object UniverseComponent {
  val All: Iterable[UniverseComponent] = track(classOf[UniverseComponent]).asScala
}

class UniverseComponent(entity: Entity, multiverse: Multiverse) extends Component(entity) {

  var position: Option[Id[Cell]] = None
  var primaryBit: Option[Id[Boolean]] = None

  var blockingCells: Universe => List[Cell] = _ => List()

}
