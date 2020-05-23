package superposition.game

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.{TiledMap, TiledMapTileLayer}
import com.badlogic.gdx.maps.{MapLayer, MapObject, MapProperties}
import superposition.component.{Multiverse, MultiverseView}
import superposition.entity.{MapLayer => MapLayerEntity, _}
import superposition.game.ResourceResolver.resolve
import superposition.language.Interpreter
import superposition.math._

import scala.Function.const
import scala.jdk.CollectionConverters._
import scala.sys.error

/** Loads game levels. */
private object LevelLoader {
  /** Loads a level from the tile map.
    *
    * @param map the tile map
    * @return the level
    */
  def loadLevel(map: TiledMap): Level = {
    // Spawn object entities.
    val multiverse = new Multiverse(walls(map))
    for (entity <- getEntities(multiverse, map)) {
      println(s"Spawning ${entity.getClass.getSimpleName}.")
      multiverse.addEntity(entity)
    }

    // Apply initial gates.
    for (gates <- Option(map.getProperties.get("Gates", classOf[String]))) {
      multiverse.applyGate(new Interpreter(multiverse, map).evalGate(gates), ())
    }

    // Create the map renderer.
    val camera = new OrthographicCamera(map.getProperties.get("width", classOf[Int]),
                                        map.getProperties.get("height", classOf[Int]))
    camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0)
    camera.update()
    val shader = new ShaderProgram(resolve("shaders/sprite.vert"), resolve("shaders/spriteMixColor.frag"))
    assert(shader.isCompiled, shader.getLog)
    val batch = new SpriteBatch(1000, shader)
    val renderer = new OrthogonalTiledMapRenderer(map, 1 / 16f, batch)
    renderer.setView(camera)

    val layers = layerEntities(map, renderer, multiverse) ++ Iterable(new CellHighlighter(1))
    new Level(multiverse, new MultiverseView(multiverse, camera), layers, shader, batch)
  }

  /** Returns entities for all visible tile map layers.
    *
    * @param map the tile map
    * @param renderer the map renderer
    * @param multiverse the multiverse
    * @return entities for all visible tile map layers
    */
  private def layerEntities(map: TiledMap,
                            renderer: OrthogonalTiledMapRenderer,
                            multiverse: Multiverse): Iterable[MapLayerEntity] =
    map
      .getLayers
      .getByType(classOf[TiledMapTileLayer])
      .asScala
      .filter(_.isVisible)
      .zipWithIndex
      .map(layerEntity(map, renderer, multiverse).tupled)

  /** Returns the entity for a tile map layer.
    *
    * @param map the tile map
    * @param renderer the tile map renderer
    * @param multiverse the multiverse
    * @param mapLayer the map layer
    * @param index the map layer index
    * @return the tile map layer entity
    */
  private def layerEntity(map: TiledMap, renderer: OrthogonalTiledMapRenderer, multiverse: Multiverse)
                         (mapLayer: MapLayer, index: Int): MapLayerEntity = {
    val renderLayer = Option(mapLayer.getProperties.get("Layer", classOf[Int])).getOrElse(0)
    val control = controlFunction(multiverse, map, mapLayer.getProperties)
    new MapLayerEntity(renderer, renderLayer, index, multiverse, control)
  }

  /** Returns the entities for all of the objects in the tile map.
    *
    * @param multiverse the multiverse
    * @param map the tile map
    * @return a mapping from object ID to entity
    */
  private def getEntities(multiverse: Multiverse, map: TiledMap): Iterable[Entity] =
    for {
      layer <- map.getLayers.asScala
      obj <- layer.getObjects.asScala
    } yield objectEntity(multiverse, map, obj)

  /** Returns the entity for a tile map object.
    *
    * @param multiverse the multiverse
    * @param map the tile map
    * @param obj the map object
    * @return the object entity
    */
  private def objectEntity(multiverse: Multiverse, map: TiledMap, obj: MapObject): Entity = {
    val id = obj.getProperties.get("id", classOf[Int])
    val cells = objectCells(map, obj)
    obj.getProperties.get("type") match {
      case "Player" => new Cat(id, multiverse, cells.head)
      case "Quball" => new Quball(id, multiverse, cells.head)
      case "QuballMulti" => new QuballMulti(id, multiverse, cells.head, 4)
      case "Laser" =>
        val gate = toGate(obj.getProperties.get("Gate", classOf[String]))
        val direction = Direction.withName(obj.getProperties.get("Direction", classOf[String]))
        if (obj.getProperties.containsKey("ControlsMulti")) {
          val control = controlFunctionBitSeq(multiverse, map, obj.getProperties)
          new Laser(multiverse, cells.head, gate, direction, control)
        } else {
          val control = controlFunction(multiverse, map, obj.getProperties)
          new Laser(multiverse, cells.head, gate, direction, u => new BitSeq(Seq(control(u))))
        }
      case "Door" =>
        val control = controlFunction(multiverse, map, obj.getProperties)
        new Door(multiverse, cells.head, control)
      case "Exit" => new Exit(cells)
      case unknown => error(s"Unknown entity type '$unknown'.")
    }
  }

  /** Returns the control function for the tile map object.
    *
    * @param multiverse the multiverse
    * @param map the tile map
    * @param prop the tile map object properties
    * @return the control function for the tile map object
    */
  private def controlFunction(multiverse: Multiverse, map: TiledMap, prop: MapProperties): Universe => Boolean =
    Option(prop.get("Controls", classOf[String]))
      .map(new Interpreter(multiverse, map).evalExpression)
      .getOrElse(const(true))

  /** Returns the control function for the tile map object.
    *
    * @param multiverse the multiverse
    * @param map the tile map
    * @param prop the tile map object properties
    * @return the control function for the tile map object
    */
  private def controlFunctionBitSeq(multiverse: Multiverse, map: TiledMap, prop: MapProperties): Universe => BitSeq =
    Option(prop.get("ControlsMulti", classOf[String]))
      .map(new Interpreter(multiverse, map).evalExpression)
      .getOrElse(const(new BitSeq(Seq(true))))

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
  private def toGate(name: String): Gate[StateId[Boolean]] = name match {
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
