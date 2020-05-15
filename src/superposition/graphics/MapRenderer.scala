package superposition.graphics

import com.badlogic.ashley.core.{Entity, Family}
import superposition.game.component.MapView

/** Renders tile maps. */
object MapRenderer extends Renderer {
  override val family: Family = Family.all(classOf[MapView]).get

  override def render(entity: Entity, deltaTime: Float): Unit = {
    val mapView = MapView.Mapper.get(entity)
    mapView.renderer.render(mapView.layers)
  }
}
