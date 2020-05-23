package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import superposition.math.QExpr

/** The map layer view component contains rendering data for a tile map layer.
  *
  * When there are no control cells, or all of the layer's control cells contain a qubit in the on state, the layer is
  * rendered normally. Otherwise, the layer color is black.
  *
  * @param renderer the tile map renderer
  * @param layer the tile map layer index to render
  * @param control the control for the layer
  */
final class MapLayerView(
    val renderer: OrthogonalTiledMapRenderer,
    val layer: Int,
    val control: QExpr[Boolean])
  extends Component

/** Contains the component mapper for the map layer view component. */
object MapLayerView {
  /** The component mapper for the map layer view component. */
  val mapper: ComponentMapper[MapLayerView] = ComponentMapper.getFor(classOf[MapLayerView])
}
