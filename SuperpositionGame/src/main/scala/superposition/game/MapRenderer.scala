package superposition.game

import com.badlogic.ashley.core._
import superposition.game.MapRenderer.MapComponent

import scala.jdk.CollectionConverters._

private final class MapRenderer extends EntitySystem {
  private var entities: Iterable[Entity] = Nil

  override def addedToEngine(engine: Engine): Unit =
    entities = engine.getEntitiesFor(Family.all(classOf[MapComponent]).get).asScala

  override def update(deltaTime: Float): Unit =
    entities.foreach(MapComponent.get(_).renderer.render())
}

private object MapRenderer {
  private val MapComponent: ComponentMapper[MapComponent] = ComponentMapper.getFor(classOf[MapComponent])
}
