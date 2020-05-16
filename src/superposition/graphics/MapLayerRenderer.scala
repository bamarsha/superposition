package superposition.graphics

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.gdx.graphics.Color.{BLACK, WHITE}
import superposition.game.component.{MapLayerView, Renderable}
import superposition.game.entity.Level
import superposition.graphics.Extensions._

/** Renders tile map layers.
  *
  * @param level a function that returns the current level
  */
final class MapLayerRenderer(level: () => Option[Level]) extends Renderer {
  override val family: Family = Family.all(classOf[MapLayerView]).get

  override def render(entity: Entity, deltaTime: Float): Unit = {
    val multiverse = level().get.multiverse
    val multiverseView = level().get.multiverseView
    val mapView = MapLayerView.Mapper.get(entity)
    val shader = mapView.renderer.getBatch.getShader
    multiverseView.enqueueRenderer(Renderable.Mapper.get(entity).dependentState) { (universe, renderParams) =>
      shader.begin()
      shader.setUniformColor("color", if (multiverse.allOn(universe, mapView.controls)) WHITE else BLACK)
      shader.setUniformColor("tintColor", renderParams.color)
      shader.end()
      mapView.renderer.render(Array(mapView.layer))
    }
  }
}
