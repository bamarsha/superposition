package superposition.game

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.{TiledMap, TiledMapTileLayer}
import com.badlogic.gdx.maps.{MapLayer, MapObject}
import superposition.component.{Multiverse, MultiverseView, PrimaryBit}
import superposition.entity.{MapLayer => MapLayerEntity, _}
import superposition.game.ResourceResolver.resolve
import superposition.math._

import scala.collection.immutable.HashMap
import scala.jdk.CollectionConverters._
import scala.sys.error

/** Functions for loading levels. */
private object LevelLoader {
  /** Loads a level from the tile map.
    *
    * @param map the tile map
    * @return the level
    */
  def loadLevel(map: TiledMap): Level = {
    val multiverse = new Multiverse(walls(map))

    // Make object entities.
    var objects = new HashMap[Int, Entity]
    for (layer <- map.getLayers.asScala; obj <- layer.getObjects.asScala) {
      println(s"Making ${obj.getName} (${obj.getProperties.get("type")}).")
      val entity = makeObjectEntity(multiverse, map, obj)
      multiverse.addEntity(entity)
      objects += obj.getProperties.get("id", classOf[Int]) -> entity
      // TODO: entity.layer = layer
    }

    // Apply gates.
    if (map.getProperties.containsKey("Gates")) {
      val gates = map.getProperties.get("Gates", classOf[String])
      for (Array(name, target) <- gates.linesIterator map (_.split(' '))) {
        println(s"Applying gate $name on object $target.")
        val bit = objects(target.toInt).getComponent(classOf[PrimaryBit]).bit
        multiverse.applyGate(makeGate(name), bit)
      }
    }

    val camera = new OrthographicCamera(map.getProperties.get("width", classOf[Int]),
                                        map.getProperties.get("height", classOf[Int]))
    camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0)
    camera.update()
    val shader = new ShaderProgram(resolve("shaders/sprite.vert"), resolve("shaders/spriteMixColor.frag"))
    val batch = new SpriteBatch(1000, shader)
    val mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / 16f, batch)
    mapRenderer.setView(camera)
    val layers = map.getLayers.asScala.zipWithIndex.map(makeLayerEntity(multiverse, map, mapRenderer).tupled)
    new Level(multiverse,
              new MultiverseView(multiverse, camera),
              layers ++ Iterable(new CellHighlighter(1)),
              shader,
              batch)
  }

  /** Makes an entity from a tile map layer.
    *
    * @param multiverse the multiverse
    * @param map the tile map
    * @param mapRenderer the tile map renderer
    * @param mapLayer the map layer
    * @param index the map layer index
    * @return the layer entity
    */
  private def makeLayerEntity(multiverse: Multiverse, map: TiledMap, mapRenderer: OrthogonalTiledMapRenderer)
                             (mapLayer: MapLayer, index: Int): Entity = {
    val renderLayer = Option(mapLayer.getProperties.get("Layer", classOf[Int])).getOrElse(0)
    val controls = Option(mapLayer.getProperties.get("Controls", classOf[String])).toSeq flatMap parseCells(map)
    new MapLayerEntity(mapRenderer, renderLayer, index, multiverse, controls)
  }

  /** Makes an entity from a tile map object.
    *
    * @param multiverse the multiverse
    * @param map the tile map
    * @param obj the map object
    * @return the object entity
    */
  private def makeObjectEntity(multiverse: Multiverse, map: TiledMap, obj: MapObject): Entity = {
    val cells = objectCells(map, obj)
    obj.getProperties.get("type") match {
      case "Player" => new Cat(multiverse, cells.head)
      case "Quball" => new Quball(multiverse, cells.head)
      case "Laser" =>
        val gate = makeGate(obj.getProperties.get("Gate", classOf[String]))
        val direction = Direction.withName(obj.getProperties.get("Direction", classOf[String]))
        val control =
          if (obj.getProperties.containsKey("Controls"))
            parseCells(map)(obj.getProperties.get("Controls", classOf[String])).toList
          else Nil
        new Laser(multiverse, cells.head, gate, direction, control)
      case "Door" =>
        val controls = parseCells(map)(obj.getProperties.get("Controls", classOf[String])).toList
        new Door(multiverse, cells.head, controls)
      case "Exit" => new Exit(cells)
      case unknown => error(s"Unknown entity type '$unknown'.")
    }
  }

  /** Parses a cell position for the tile map from a string "(x, y)".
    *
    * @param map the tile map
    * @param string the cell position string
    * @return the cell position
    */
  private def parseCell(map: TiledMap)(string: String): Vector2[Int] = {
    val height = map.getProperties.get("height", classOf[Int])
    """\((\d+),\s*(\d+)\)""".r("x", "y").findFirstMatchIn(string) match {
      case Some(m) => Vector2(m.group("x").trim.toInt, height - m.group("y").trim.toInt - 1)
      case None => error(s"Invalid cell '$string'.")
    }
  }

  /** Parses a sequence of cell positions for the tile map from a string "(x_1, y_1)\n...\n(x_n, y_n)".
    *
    * @param map the tile map
    * @param string the cell positions string
    * @return the cell positions
    */
  private def parseCells(map: TiledMap)(string: String): Seq[Vector2[Int]] =
    (string.linesIterator map parseCell(map)).toSeq

  /** Returns the gate corresponding to the gate name.
    *
    * @param name the gate name
    * @return the corresponding gate
    */
  private def makeGate(name: String): Gate[StateId[Boolean]] = name match {
    case "X" => X
    case "Z" => Z
    case "H" => H
    case _ => error(s"Unsupported gate '$name'.")
  }

  /** The set of cells a map object occupies based on its x and y coordinates and its width and height.
    *
    * @param map the tile map
    * @param obj the map object
    * @return the set of cells occupied by the object
    */
  private def objectCells(map: TiledMap, obj: MapObject): Set[Vector2[Int]] = {
    val tileWidth = map.getProperties.get("tilewidth", classOf[Int])
    val tileHeight = map.getProperties.get("tileheight", classOf[Int])
    val bottomLeft =
      (Vector2(obj.getProperties.get("x", classOf[Float]) / tileWidth,
               obj.getProperties.get("y", classOf[Float]) / tileHeight)
        map (_.floor.toInt))
    val topRight = bottomLeft +
      (Vector2(obj.getProperties.get("width", classOf[Float]) / tileWidth,
               obj.getProperties.get("height", classOf[Float]) / tileHeight)
        map (_.floor.toInt.max(1)))
    (for {
      x <- bottomLeft.x until topRight.x
      y <- bottomLeft.y until topRight.y
    } yield Vector2(x, y)).toSet
  }

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
