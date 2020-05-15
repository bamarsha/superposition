package superposition.game

import com.badlogic.ashley.core._
import superposition.game.component.MapView

/** Renders tile maps. */
private object MapRenderer extends Renderer {
  override val family: Family = Family.all(classOf[MapView]).get

  override def render(entity: Entity, deltaTime: Float): Unit = {
    val mapView = MapView.Mapper.get(entity)
    mapView.renderer.render(mapView.layers)
  }
}
