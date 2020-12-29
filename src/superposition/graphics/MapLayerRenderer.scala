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
    val multiverseView = level().get.multiverseView
    val mapView = MapLayerView.mapper.get(entity)
    val shader = mapView.renderer.getBatch.getShader
    multiverseView.enqueueRenderer(Renderable.mapper.get(entity)) { (universe, renderInfo) =>
      shader.bind()
      val drawOn = mapView.control(universe)
      val tintColor = renderInfo.color.cpy().mul(1, 1, 1, if (drawOn) 2 else .5f)
      shader.setUniformColor("color", if (drawOn) WHITE else BLACK, colorArray)
      shader.setUniformColor("tintColor", tintColor, colorArray)
      mapView.renderer.render(Array(mapView.layer))
    }
  }
}
