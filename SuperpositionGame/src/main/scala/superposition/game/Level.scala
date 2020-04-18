package superposition.game

import engine.core.Behavior.Entity
import engine.core.Game
import engine.core.Input.keyJustPressed
import engine.graphics.Camera
import extras.tiles.Tilemap
import org.lwjgl.glfw.GLFW.GLFW_KEY_R
import superposition.math.{Direction, Vec2i}
import superposition.quantum._

import scala.collection.immutable.HashMap
import scala.jdk.CollectionConverters._
import scala.sys.error

/**
 * Manages game levels.
 */
private object Level {
  private var createMultiverse: () => Option[Multiverse] = () => None

  private var multiverse: Option[Multiverse] = None

  /**
   * Declares the level system.
   */
  def declareSystem(): Unit =
    Game.declareSystem { () =>
      if (keyJustPressed(GLFW_KEY_R)) {
        multiverse.foreach(Game.destroy)
        multiverse = createMultiverse()
        multiverse.foreach(Game.create)
      }
    }

  /**
   * Loads the multiverse as a new level.
   *
   * @param multiverse the multiverse
   */
  def load(multiverse: => Multiverse): Unit = {
    this.multiverse.foreach(Game.destroy)

    val current = multiverse
    Camera.camera2d.lowerLeft = current.boundingBox.lowerLeft
    Camera.camera2d.upperRight = current.boundingBox.upperRight
    Game.create(current)

    createMultiverse = () => Some(multiverse)
    this.multiverse = Some(current)
  }

  /**
   * Loads the tile map as a new level.
   *
   * @param tileMap the tile map to load
   */
  def load(tileMap: Tilemap): Unit =
    load {
      val multiverse = new Multiverse(tileMap)
      var entities = new HashMap[Int, Entity]

      for ((group, layer) <- tileMap.objectGroups.asScala.zipWithIndex;
           obj <- group.objects.asScala) {
        val entity = entityFromObject(tileMap, obj, multiverse)
        println("Spawning " + obj.`type` + ".")
        multiverse.addEntity(entity)
        entities += obj.id -> entity
        // TODO: entity.layer = layer
      }

      val gates = tileMap.properties.asScala.get("Gates")
      for (Array(gateName, targetId) <- gates.iterator flatMap (_.value.linesIterator) map (_.split(' '))) {
        println("Applying gate " + gateName + " on entity " + targetId + ".")
        val target = entities(targetId.toInt).get(classOf[UniverseComponent]).primaryBit.get
        multiverse.applyGate(gateFromName(gateName), target)
      }

      multiverse
    }

  private def entityFromObject(tileMap: Tilemap, obj: Tilemap#ObjectGroup#Object, multiverse: Multiverse): Entity = {
    val cell = Vec2i(
      (obj.x / tileMap.tileWidth).floor.toInt,
      tileMap.height - (obj.y / tileMap.tileHeight).floor.toInt - 1)
    val properties = obj.properties.asScala
    obj.`type` match {
      case "Player" => new Player(multiverse, cell)
      case "Quball" => new Quball(multiverse, cell)
      case "Laser" =>
        val gate = gateFromName(properties("Gate").value)
        val direction = Direction.withName(properties("Direction").value)
        val control = (properties.get("Control") map (control => cellFromString(tileMap, control.value))).toList
        new Laser(multiverse, cell, gate, direction, control)
      case "Door" =>
        val controls = cellsFromString(tileMap, properties("Controls").value).toList
        new Door(multiverse, cell, controls)
      case "Goal" =>
        // TODO: val requires = ObjectId(properties("Requires").value.toInt)
        val next = properties("Next Level").value
        new Goal(multiverse, cell,
          required = Player.All.head.cell,
          action = () => load(Tilemap.load(getClass.getResource(next))))
      case _ => error("Unknown entity type '" + obj.`type` + "'.")
    }
  }

  private def cellFromString(tileMap: Tilemap, string: String): Vec2i =
    """\((\d+),\s*(\d+)\)""".r("x", "y").findFirstMatchIn(string) match {
      case Some(m) => Vec2i(m.group("x").trim.toInt, tileMap.height - m.group("y").trim.toInt - 1)
      case None => error("Invalid cell '" + string + "'.")
    }

  private def cellsFromString(tileMap: Tilemap, string: String): Seq[Vec2i] =
    (string.linesIterator map (cellFromString(tileMap, _))).toSeq

  private def gateFromName(name: String): Gate[StateId[Boolean]] = name match {
    case "X" => X
    case "H" => H
    case _ => error("Unsupported gate '" + name + "'.")
  }
}
