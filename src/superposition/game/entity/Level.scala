package superposition.game.entity

import com.badlogic.ashley.core._
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.{TiledMap, TiledMapTileLayer}
import superposition.game.component.{MapView, Multiverse, MultiverseView}
import superposition.game.entity.Level.walls
import superposition.math.Vector2i

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

  add(new MapView(map, camera))
  add(multiverse)
  add(multiverseView)
}

/** Functions for extracting information from tile maps. */
private object Level {
  /** Returns the set of walls, or cells with collision, in the tile map.
    *
    * @param map the tile map
    * @return the set of walls in the tile map
    */
  private def walls(map: TiledMap): Set[Vector2i] =
    (for {
      layer <- map.getLayers.asScala
      if layer.isInstanceOf[TiledMapTileLayer] && hasCollision(layer)
      tiledLayer = layer.asInstanceOf[TiledMapTileLayer]
      x <- 0 until tiledLayer.getWidth
      y <- 0 until tiledLayer.getHeight
      if hasTileAt(tiledLayer, x, y)
    } yield Vector2i(
      (x + layer.getOffsetX.toDouble / map.getProperties.get("tilewidth", classOf[Int])).round.toInt,
      (y + layer.getOffsetY.toDouble / map.getProperties.get("tileheight", classOf[Int])).round.toInt))
      .toSet

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
