package superposition.game.entity

import com.badlogic.ashley.core._
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.{TiledMap, TiledMapTileLayer}
import superposition.game.component.{MapView, Multiverse, MultiverseView}
import superposition.game.entity.Level.walls
import superposition.math.Vector2i

import scala.jdk.CollectionConverters._

/**
  * The multiverse is a collection of universes.
  *
  * Multiple universes represent qubits in superposition. The multiverse can apply quantum gates to qubits by changing
  * the amplitude of a universe or creating a copy of a universe.
  */
final class Level(map: TiledMap) extends Entity {
  private val camera: OrthographicCamera = new OrthographicCamera(
    map.getProperties.get("width", classOf[Int]),
    map.getProperties.get("height", classOf[Int]))
  camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0)
  camera.update()

  val multiverse: Multiverse = new Multiverse(walls(map))

  val multiverseView: MultiverseView = new MultiverseView(multiverse, camera)

  add(new MapView(map, camera))
  add(multiverse)
  add(multiverseView)
}

private object Level {
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

  private def hasCollision(layer: MapLayer): Boolean =
    layer.getProperties.containsKey("Collision") && layer.getProperties.get("Collision", classOf[Boolean])

  private def hasTileAt(layer: TiledMapTileLayer, x: Int, y: Int): Boolean = Option(layer.getCell(x, y)).isDefined
}
