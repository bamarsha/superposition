package superposition.game.entity

import com.badlogic.ashley.core._
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.{TiledMap, TiledMapTileLayer}
import superposition.game.component.{MapView, Multiverse, MultiverseView, Renderable}
import superposition.game.entity.Level.walls
import superposition.math.Vector2

import scala.Function.const
import scala.jdk.CollectionConverters._

/** A quantum level with a multiverse and tile map.
  *
  * @param map the tile map
  */
final class Level(map: TiledMap) extends Entity {
  /** The camera with which to draw the level. */
  private val camera: OrthographicCamera = new OrthographicCamera(
    map.getProperties.get("width", classOf[Int]),
    map.getProperties.get("height", classOf[Int]))
  camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0)
  camera.update()

  /** The multiverse for the level. */
  val multiverse: Multiverse = new Multiverse(walls(map))

  /** The view of the multiverse for the level. */
  val multiverseView: MultiverseView = new MultiverseView(multiverse, camera)

  add(multiverse)
  add(multiverseView)
  add(new Renderable(1, const(())))

  /** The tile map renderer. */
  private val mapRenderer: OrthogonalTiledMapRenderer = new OrthogonalTiledMapRenderer(map, 1 / 16f)
  mapRenderer.setView(camera)

  /** The renderable entities corresponding to each tile map layer. */
  val mapLayers: Iterable[Entity] = map
    .getLayers
    .asScala
    .zipWithIndex
    .groupBy { case (mapLayer, _) =>
      Option(mapLayer.getProperties.get("Layer", classOf[Int])).getOrElse(0)
    }
    .map { case (renderableLayer, mapLayers) =>
      MapView.makeEntity(mapRenderer, renderableLayer, (mapLayers map (_._2)).toArray)
    }
}

/** Functions for extracting information from tile maps. */
private object Level {
  /** Returns the set of walls, or cells with collision, in the tile map.
    *
    * @param map the tile map
    * @return the set of walls in the tile map
    */
  private def walls(map: TiledMap): Set[Vector2[Int]] =
    (map.getLayers.asScala flatMap {
      case layer: TiledMapTileLayer if hasCollision(layer) =>
        for {
          x <- 0 until layer.getWidth
          y <- 0 until layer.getHeight
          if hasTileAt(layer, x, y)
          cellX = (x + layer.getOffsetX / map.getProperties.get("tilewidth", classOf[Int])).round
          cellY = (y + layer.getOffsetY / map.getProperties.get("tileheight", classOf[Int])).round
        } yield Vector2(cellX, cellY)
      case _ => Nil
    }).toSet

  /** Returns true if the tile map layer has collision.
    *
    * @param layer the tile map layer
    * @return true if the tile map layer has collision
    */
  private def hasCollision(layer: MapLayer): Boolean =
    layer.getProperties.containsKey("Collision") && layer.getProperties.get("Collision", classOf[Boolean])

  /** Returns true if the tile map layer has a tile at the position.
    *
    * @param layer the tile map layer
    * @param x the x coordinate
    * @param y the y coordinate
    * @return true if the tile map layer has a tile at the position
    */
  private def hasTileAt(layer: TiledMapTileLayer, x: Int, y: Int): Boolean = Option(layer.getCell(x, y)).isDefined
}
