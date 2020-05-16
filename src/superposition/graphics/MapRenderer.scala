package superposition.graphics

import com.badlogic.ashley.core.{Entity, Family}
import superposition.game.component.MapView
import superposition.game.entity.Level

import scala.Function.const

/** Renders tile maps.
  *
  * @param level a function that returns the current level
  */
final class MapRenderer(level: () => Option[Level]) extends Renderer {
  override val family: Family = Family.all(classOf[MapView]).get

  override def render(entity: Entity, deltaTime: Float): Unit = {
    val multiverseView = level().get.multiverseView
    val mapView = MapView.Mapper.get(entity)
    multiverseView.enqueueRenderer(const(())) { (_, _) =>
      mapView.renderer.render(mapView.layers)
    }
  }
}
