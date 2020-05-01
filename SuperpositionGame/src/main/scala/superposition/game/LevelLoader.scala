package superposition.game

import com.badlogic.ashley.core.{Engine, Entity}
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import superposition.math.{Direction, Vector2i}
import superposition.quantum._

import scala.collection.immutable.HashMap
import scala.jdk.CollectionConverters._
import scala.sys.error

/**
 * Loads game levels.
 */
private final class LevelLoader(engine: Engine) {
  private var makeMultiverse: () => Option[Level] = () => None

  private var currentMultiverse: Option[Level] = None

//  /**
//   * Declares the level system.
//   */
//  def declareSystem(): Unit =
//    Game.declareSystem { () =>
//      if (keyJustPressed(GLFW_KEY_R)) {
//        multiverse.foreach(Game.destroy)
//        multiverse = createMultiverse()
//        multiverse.foreach(Game.create)
//      }
//    }

  /**
   * Loads the multiverse as a new level.
   *
   * @param makeMultiverse a function that makes a new instance of the multiverse
   */
  private def load(makeMultiverse: () => Level): Unit = {
    this.makeMultiverse = () => Some(makeMultiverse())
    // TODO: Remove all entities in the multiverse.
    currentMultiverse.foreach(engine.removeEntity)
    currentMultiverse = this.makeMultiverse()
    currentMultiverse.foreach(engine.addEntity)
  }

  /**
   * Loads the tile map as a new level.
   *
   * @param map the tile map to load
   */
  def load(map: TiledMap): Unit = load(() => {
    val multiverse = new Level(map)
    var entities = new HashMap[Int, Entity]

    for (layer <- map.getLayers.asScala;
         obj <- layer.getObjects.asScala) {
      println(s"Spawning ${obj.getName} (${obj.getProperties.get("type")}).")

      val entity = makeEntity(multiverse.getComponent(classOf[Multiverse]), map, obj)
      // todo entities shouldn't be null
      if (entity != null) {
        engine.addEntity(entity)
        multiverse.getComponent(classOf[Multiverse]).addEntity(entity)
      }
      entities += obj.getProperties.get("id", classOf[Int]) -> entity
      // TODO: entity.layer = layer
    }

//      val gates = tileMap.properties.asScala.get("Gates")
//      for (Array(gateName, targetId) <- gates.iterator flatMap (_.value.linesIterator) map (_.split(' '))) {
//        println("Applying gate " + gateName + " on entity " + targetId + ".")
//        val target = entities(targetId.toInt).get(classOf[UniverseComponent]).primaryBit.get
//        multiverse.applyGate(gateFromName(gateName), target)
//      }
    multiverse
  })

  private def makeEntity(multiverse: Multiverse, map: TiledMap, obj: MapObject): Entity = {
    val height = map.getProperties.get("height", classOf[Int])
    val tileWidth = map.getProperties.get("tilewidth", classOf[Int])
    val tileHeight = map.getProperties.get("tileheight", classOf[Int])
    val x = obj.getProperties.get("x", classOf[Float])
    val y = obj.getProperties.get("y", classOf[Float])
    val cell = Vector2i((x / tileWidth).floor.toInt, (y / tileHeight).floor.toInt)
    obj.getProperties.get("type") match {
      case "Player" => println("would make player"); null //new Player(multiverse, cell)
      case "Quball" => new Quball(multiverse, cell)
      case "Laser" =>
        val gate = makeGate(obj.getProperties.get("Gate", classOf[String]))
        val direction = Direction.withName(obj.getProperties.get("Direction", classOf[String]))
//        val control = (properties.get("Control") map (control => cellFromString(tileMap, control.value))).toList
        println("would make laser")
        null
//        new Laser(multiverse, cell, gate, direction, control)
      case "Door" =>
        val controls = makeCells(obj.getProperties.get("Controls", classOf[String])).toList
        new Door(multiverse, cell, controls)
      case "Goal" =>
        // TODO: val requires = ObjectId(properties("Requires").value.toInt)
        // TODO: val next = properties("Next Level").value
        new Goal(multiverse,
                 cell,
                 required = null, // TODO: Player.All.head.cell,
                 action = () => println("yay")) // TODO: () => load(Tilemap.load(getClass.getResource(next))))
      case unknown => error(s"Unknown entity type $unknown.")
    }
  }

  private def makeCell(string: String): Vector2i =
    """\((\d+),\s*(\d+)\)""".r("x", "y").findFirstMatchIn(string) match {
      case Some(m) => Vector2i(m.group("x").trim.toInt, m.group("y").trim.toInt)
      case None => error("Invalid cell '" + string + "'.")
    }

  private def makeCells(string: String): Seq[Vector2i] = (string.linesIterator map makeCell).toSeq

  private def makeGate(name: String): Gate[StateId[Boolean]] = name match {
    case "X" => X
    case "Z" => Z
    case "H" => H
    case _ => error(s"Unsupported gate '$name'.")
  }
}
