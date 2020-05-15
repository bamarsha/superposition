package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper, Entity}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer

import scala.Function.const

/** The map view component contains a renderer for a tile map.
  *
  * @param renderer the tile map renderer
  * @param layers the tile map layer indices to render
  */
final class MapView(val renderer: OrthogonalTiledMapRenderer, val layers: Array[Int]) extends Component

/** Contains the component mapper for the map view component. */
object MapView {
  /** The component mapper for the map view component. */
  val Mapper: ComponentMapper[MapView] = ComponentMapper.getFor(classOf[MapView])

  /** Creates a renderable map view entity.
    *
    * @param renderer the tile map renderer
    * @param renderableLayer the renderable layer to render the tile map in
    * @param mapLayers the tile map layer indices to render
    * @return the renderable map view entity
    */
  def makeEntity(renderer: OrthogonalTiledMapRenderer, renderableLayer: Int, mapLayers: Array[Int]): Entity =
    (new Entity)
      .add(new Renderable(renderableLayer, const(())))
      .add(new MapView(renderer, mapLayers))
}
