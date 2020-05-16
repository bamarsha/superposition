package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper, Entity}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import superposition.math.Vector2

/** The map layer view component contains rendering data for a tile map layer.
  *
  * When there are no control cells, or all of the layer's control cells contain a qubit in the on state, the layer is
  * rendered normally. Otherwise, the layer color is black.
  *
  * @param renderer the tile map renderer
  * @param layer the tile map layer index to render
  * @param controls the control cells for the layer
  */
final class MapLayerView(
    val renderer: OrthogonalTiledMapRenderer,
    val layer: Int,
    val controls: Iterable[Vector2[Int]])
  extends Component

/** Contains the component mapper for the map layer view component. */
object MapLayerView {
  /** The component mapper for the map layer view component. */
  val Mapper: ComponentMapper[MapLayerView] = ComponentMapper.getFor(classOf[MapLayerView])

  /** Makes a renderable map layer entity.
    *
    * @param multiverse the multiverse
    * @param renderer the tile map renderer
    * @param renderLayer the render layer to render the tile map in
    * @param mapLayer the tile map layer index to render
    * @return the renderable map view entity
    */
  def makeEntity(multiverse: Multiverse,
                 renderer: OrthogonalTiledMapRenderer,
                 renderLayer: Int,
                 mapLayer: Int,
                 controls: Iterable[Vector2[Int]]): Entity =
    (new Entity)
      .add(new Renderable(renderLayer, multiverse.allOn(_, controls)))
      .add(new MapLayerView(renderer, mapLayer, controls))
}
