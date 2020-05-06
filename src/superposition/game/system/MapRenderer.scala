package superposition.game.system

import com.badlogic.ashley.core._
import superposition.game.component.MapView

import scala.jdk.CollectionConverters._

final class MapRenderer extends EntitySystem {
  private var entities: Iterable[Entity] = Nil

  override def addedToEngine(engine: Engine): Unit =
    entities = engine.getEntitiesFor(Family.all(classOf[MapView]).get).asScala

  override def update(deltaTime: Float): Unit =
    entities.foreach(MapView.Mapper.get(_).renderer.render())
}
