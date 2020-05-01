package superposition.game

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer

private final class MapComponent(map: TiledMap, camera: OrthographicCamera) extends Component {
  val renderer: OrthogonalTiledMapRenderer = new OrthogonalTiledMapRenderer(map, 1 / 16f)
  renderer.setView(camera)
}
