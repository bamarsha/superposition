package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer

/** The map view component stores a renderer for a tile map.
  *
  * @param map the tile map to render
  * @param camera the camera with which to view the tile map
  */
final class MapView(map: TiledMap, camera: OrthographicCamera) extends Component {
  /** The tile map renderer. */
  val renderer: OrthogonalTiledMapRenderer = new OrthogonalTiledMapRenderer(map, 1 / 16f)
  renderer.setView(camera)
}

/** Contains the component mapper for the map view component. */
object MapView {
  /** The component mapper for the map view component. */
  val Mapper: ComponentMapper[MapView] = ComponentMapper.getFor(classOf[MapView])
}
