package superposition.graphics

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.gdx.graphics.Color.{BLACK, WHITE}
import superposition.component.{MapLayerView, Renderable}
import superposition.entity.Level
import superposition.graphics.ColorUtils.ShaderOps

/** Renders tile map layers.
  *
  * @param level a function that returns the current level
  */
final class MapLayerRenderer(level: () => Option[Level]) extends Renderer {
  /** An array for holding color components. */
  private val colorArray: Array[Float] = Array.ofDim(4)

  override val family: Family = Family.all(classOf[MapLayerView]).get

  override def render(entity: Entity, deltaTime: Float): Unit = {
    val multiverse = level().get.multiverse
    val multiverseView = level().get.multiverseView
    val mapView = MapLayerView.Mapper.get(entity)
    val shader = mapView.renderer.getBatch.getShader
    val dependentState = Renderable.Mapper.get(entity).dependentState
    multiverseView.enqueueRenderer(dependentState) { (universe, renderInfo) =>
      shader.begin()
      val allOn = multiverse.allOn(universe, mapView.controls)
      val tintColor = renderInfo.color.cpy().mul(1, 1, 1, if (allOn) 2 else .5f)
      shader.setUniformColor("color", if (allOn) WHITE else BLACK, colorArray)
      shader.setUniformColor("tintColor", tintColor, colorArray)
      shader.end()
      mapView.renderer.render(Array(mapView.layer))
    }
  }
}
