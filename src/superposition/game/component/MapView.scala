package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer

final class MapView(map: TiledMap, camera: OrthographicCamera) extends Component {
  val renderer: OrthogonalTiledMapRenderer = new OrthogonalTiledMapRenderer(map, 1 / 16f)
  renderer.setView(camera)
}

object MapView {
  val Mapper: ComponentMapper[MapView] = ComponentMapper.getFor(classOf[MapView])
}
