package superposition.game

import com.badlogic.ashley.core.{Engine, Entity}
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import superposition.game.LevelPlaylist.{LevelFactory, addLevel, makeLevel, removeLevel}
import superposition.game.component.{Multiverse, QuantumPosition, Toggle}
import superposition.game.entity._
import superposition.math.{Direction, Vector2i}
import superposition.quantum._

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.sys.error

/** A playlist of game levels.
  *
  * @param engine the entity engine
  */
private final class LevelPlaylist(engine: Engine) {
  /** The list of level factories. */
  private var factories: Seq[LevelFactory] = Nil

  /** The current level. */
  private var _current: Option[Level] = None

  /** The current level. */
  def current: Option[Level] = _current

  /** Appends the sequence of tile maps to the playlist.
    *
    * @param loader the tile map loader
    * @param fileNames the file names for the tile maps
    */
  def appendAll(loader: TmxMapLoader, fileNames: Seq[String]): Unit =
    factories ++= fileNames map (fileName => () => makeLevel(loader.load(fileName)))

  /** Advances to the next level in the playlist. */
  def next(): Unit = {
    factories = factories match {
      case Nil => Nil
      case _ :: next => next
    }
    play()
  }

  /** Plays the current level or resets the current level if it is already playing. */
  def play(): Unit = {
    current.foreach(removeLevel(engine))
    _current = factories.headOption map (_ ())
    current.foreach(addLevel(engine))
  }
}

/** Functions for loading levels. */
private object LevelPlaylist {
  /** A function that returns a new instance of a level. */
  private type LevelFactory = () => Level

  /** Adds a level and all of its entities to the engine.
    *
    * @param engine the entity engine
    * @param level the level to add
    */
  private def addLevel(engine: Engine)(level: Level): Unit = {
    engine.addEntity(level)
    level.getComponent(classOf[Multiverse]).entities.foreach(engine.addEntity)
  }

  /** Removes a level and all of its entities from the engine.
    *
    * @param engine the entity engine
    * @param level the level to remove
    */
  private def removeLevel(engine: Engine)(level: Level): Unit = {
    level.getComponent(classOf[Multiverse]).entities.foreach(engine.removeEntity)
    engine.removeEntity(level)
  }

  /** Makes a level from the tile map.
    *
    * @param map the tile map
    * @return the level
    */
  private def makeLevel(map: TiledMap): Level = {
    val level = new Level(map)
    val multiverse = level.getComponent(classOf[Multiverse])
    val entities = new mutable.HashMap[Int, Entity]

    // Spawn entities.
    for (layer <- map.getLayers.asScala; obj <- layer.getObjects.asScala) {
      println(s"Spawning ${obj.getName} (${obj.getProperties.get("type")}).")
      val entity = makeEntity(multiverse, entities, map, obj)
      multiverse.addEntity(entity)
      entities += obj.getProperties.get("id", classOf[Int]) -> entity
      // TODO: entity.layer = layer
    }

    // Apply gates.
    if (map.getProperties.containsKey("Gates")) {
      val gates = map.getProperties.get("Gates", classOf[String])
      for (Array(name, target) <- gates.linesIterator map (_.split(' '))) {
        println(s"Applying gate $name on entity $target.")
        val toggle = entities(target.toInt).getComponent(classOf[Toggle]).toggle
        multiverse.applyGate(makeGate(name), toggle)
      }
    }

    level
  }

  /** Makes an entity from a tile map object.
    *
    * @param multiverse the multiverse that the entity belongs to
    * @param entities a map from object ID to entity for each entity in the tile map
    * @param map the tile map
    * @param obj the map object
    * @return the entity
    */
  private def makeEntity(multiverse: Multiverse,
                         entities: mutable.HashMap[Int, Entity],
                         map: TiledMap,
                         obj: MapObject): Entity = {
    val tileWidth = map.getProperties.get("tilewidth", classOf[Int])
    val tileHeight = map.getProperties.get("tileheight", classOf[Int])
    val x = obj.getProperties.get("x", classOf[Float])
    val y = obj.getProperties.get("y", classOf[Float])
    val cell = Vector2i((x / tileWidth).floor.toInt, (y / tileHeight).floor.toInt)

    obj.getProperties.get("type") match {
      case "Player" => new Cat(multiverse, cell)
      case "Quball" => new Quball(multiverse, cell)
      case "Laser" =>
        val gate = makeGate(obj.getProperties.get("Gate", classOf[String]))
        val direction = Direction.withName(obj.getProperties.get("Direction", classOf[String]))
        val control =
          if (obj.getProperties.containsKey("Control"))
            List(makeCell(map)(obj.getProperties.get("Control", classOf[String])))
          else Nil
        new Laser(multiverse, cell, gate, direction, control)
      case "Door" =>
        val controls = makeCells(map)(obj.getProperties.get("Controls", classOf[String])).toList
        new Door(multiverse, cell, controls)
      case "Goal" =>
        val required = obj.getProperties.get("Requires", classOf[Int])
        new Exit(multiverse, cell, required = () => entities(required).getComponent(classOf[QuantumPosition]).cell)
      case unknown => error(s"Unknown entity type $unknown.")
    }
  }

  /** Makes a cell position for the tile map from a string "(x, y)".
    *
    * @param map the tile map
    * @param string the cell position string
    * @return the cell position
    */
  private def makeCell(map: TiledMap)(string: String): Vector2i = {
    val height = map.getProperties.get("height", classOf[Int])
    """\((\d+),\s*(\d+)\)""".r("x", "y").findFirstMatchIn(string) match {
      case Some(m) => Vector2i(m.group("x").trim.toInt, height - m.group("y").trim.toInt - 1)
      case None => error("Invalid cell '" + string + "'.")
    }
  }

  /** Makes a sequence of cell positions for the tile map from a string "(x_1, y_1)\n...\n(x_n, y_n)".
    *
    * @param map the tile map
    * @param string the cell positions string
    * @return the cell positions
    */
  private def makeCells(map: TiledMap)(string: String): Seq[Vector2i] = (string.linesIterator map makeCell(map)).toSeq

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
}
