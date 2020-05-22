package superposition.entity

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import superposition.component.{MapLayerView, Multiverse, Renderable}
import superposition.math.Vector2

/** A map layer entity.
  *
  * @param renderer the tile map renderer
  * @param renderLayer the render layer to render the tile map in
  * @param mapLayer the tile map layer index to render
  * @param multiverse the multiverse
  * @param controls the control cells for the layer
  */
final class MapLayer(
    renderer: OrthogonalTiledMapRenderer,
    renderLayer: Int,
    mapLayer: Int,
    multiverse: Multiverse,
    controls: Iterable[Vector2[Int]])
  extends Entity {
  add(new Renderable(renderLayer, multiverse.isActivated(_, controls)))
  add(new MapLayerView(renderer, mapLayer, controls))
}
