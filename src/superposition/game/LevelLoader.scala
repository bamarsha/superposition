package superposition.game

import com.badlogic.ashley.core.{Engine, Entity}
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import superposition.game.LevelLoader.{LevelFactory, addLevel, makeLevel, removeLevel}
import superposition.game.component.{Multiverse, Position, Quantum}
import superposition.game.entity._
import superposition.math.{Direction, Vector2i}
import superposition.quantum._

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.sys.error

private final class LevelLoader(engine: Engine) {
  private var playlist: Seq[LevelFactory] = Nil

  private var current: Option[Level] = None

  def startPlaylist(loader: TmxMapLoader, fileNames: Seq[String]): Unit = {
    playlist = fileNames map (fileName => () => makeLevel(loader.load(fileName)))
    resetLevel()
  }

  def nextLevel(): Unit = {
    playlist = playlist match {
      case Nil => Nil
      case _ :: next => next
    }
    resetLevel()
  }

  def resetLevel(): Unit = {
    current.foreach(removeLevel(engine))
    current = playlist.headOption map (_())
    current.foreach(addLevel(engine))
  }
}

private object LevelLoader {
  private type LevelFactory = () => Level

  private def addLevel(engine: Engine)(level: Level): Unit = {
    engine.addEntity(level)
    for (entity <- level.getComponent(classOf[Multiverse]).entities) {
      engine.addEntity(entity)
    }
  }

  private def removeLevel(engine: Engine)(level: Level): Unit = {
    for (entity <- level.getComponent(classOf[Multiverse]).entities) {
      engine.removeEntity(entity)
    }
    engine.removeEntity(level)
  }

  private def makeLevel(map: TiledMap): Level = {
    val level = new Level(map)
    val multiverse = level.getComponent(classOf[Multiverse])
    var entities = new mutable.HashMap[Int, Entity]
    for (layer <- map.getLayers.asScala; obj <- layer.getObjects.asScala) {
      println(s"Spawning ${obj.getName} (${obj.getProperties.get("type")}).")
      val entity = makeEntity(multiverse, entities, map, obj)
      multiverse.addEntity(entity)
      entities += obj.getProperties.get("id", classOf[Int]) -> entity
      // TODO: entity.layer = layer
    }
    if (map.getProperties.containsKey("Gates")) {
      val gates = map.getProperties.get("Gates", classOf[String])
      for (Array(name, target) <- gates.linesIterator map (_.split(' '))) {
        println(s"Applying gate $name on entity $target.")
        val primary = entities(target.toInt).getComponent(classOf[Quantum]).primary
        multiverse.applyGate(makeGate(name), primary)
      }
    }
    level
  }

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
        new Exit(multiverse, cell, required = () => entities(required).getComponent(classOf[Position]).cell)
      case unknown => error(s"Unknown entity type $unknown.")
    }
  }

  private def makeCell(map: TiledMap)(string: String): Vector2i = {
    val height = map.getProperties.get("height", classOf[Int])
    """\((\d+),\s*(\d+)\)""".r("x", "y").findFirstMatchIn(string) match {
      case Some(m) => Vector2i(m.group("x").trim.toInt, height - m.group("y").trim.toInt - 1)
      case None => error("Invalid cell '" + string + "'.")
    }
  }

  private def makeCells(map: TiledMap)(string: String): Seq[Vector2i] =
    (string.linesIterator map makeCell(map)).toSeq

  private def makeGate(name: String): Gate[StateId[Boolean]] = name match {
    case "X" => X
    case "Z" => Z
    case "H" => H
    case _ => error(s"Unsupported gate '$name'.")
  }
}
