package superposition.game

import engine.core.Behavior.Entity
import engine.core.{Game, Input}
import engine.graphics.Camera
import extras.tiles.Tilemap
import org.lwjgl.glfw.GLFW.GLFW_KEY_R
import superposition.math.{Cell, Direction}
import superposition.quantum.{Gate, Universe}

import scala.collection.immutable.HashMap
import scala.jdk.CollectionConverters._

/**
 * Manages game levels.
 */
private object Level {
  private var supplier: () => Option[Multiverse] = () => None

  private var multiverse: Option[Multiverse] = None

  /**
   * Declares the level system.
   */
  def declareSystem(): Unit =
    Game.declareSystem(() =>
      if (Input.keyJustPressed(GLFW_KEY_R)) {
        multiverse foreach Game.destroy
        multiverse = supplier()
        multiverse foreach Game.create
      }
    )

  /**
   * Loads the multiverse as a new level.
   *
   * @param multiverse an expression that creates a new multiverse to load
   */
  def load(multiverse: => Multiverse): Unit = {
    this.multiverse foreach Game.destroy
    supplier = () => Some(multiverse)

    val current = multiverse
    Camera.camera2d.lowerLeft = current.boundingBox.lowerLeft
    Camera.camera2d.upperRight = current.boundingBox.upperRight
    Game.create(current)

    this.multiverse = Some(current)
  }

  /**
   * Loads the tilemap as a new level.
   *
   * @param tilemap the tilemap to load
   */
  def load(tilemap: Tilemap): Unit = {
    load {
      val universe = Universe(walls = wallsInTilemap(tilemap))
      val multiverse = new Multiverse(universe, tilemap)
      var entityMap = new HashMap[Int, Entity]()

      for ((group, layer) <- tilemap.objectGroups.asScala.zipWithIndex;
           obj <- group.objects.asScala) {
        val (entity, id) = entityFromObject(tilemap, obj, multiverse)
        if (entity != null) {
          Game.create(entity)
          multiverse.entities ::= entity
          entityMap += id -> entity
        }
//        entity.layer = layer
//         universe.add(entity)
      }

      val gates = tilemap.properties.asScala.get("Gates")
      for (Array(gate, target) <- gates.iterator.flatMap(_.value.linesIterator).map(_.split(' '))) {
        println("Applying gate " + gate + " on " + target)
        // multiverse.applyGate(gate, target)
        val targetBit = entityMap.get(target.toInt).map(_.get(classOf[UniverseComponent])).flatMap(_.primaryBit)
        gate match {
          case "X" => multiverse.applyGate(Gate.X, targetBit.get)
          case "H" => multiverse.applyGate(Gate.H, targetBit.get)
        }
      }

      multiverse
    }
  }

  private def entityFromObject(tilemap: Tilemap,
                               obj: Tilemap#ObjectGroup#Object,
                               multiverse: Multiverse): (Entity, Int) = {
     val id = obj.id
//    val cell = Cell(
//      tilemap.height - (obj.y / tilemap.tileHeight).floor.toInt - 1,
//      (obj.x / tilemap.tileWidth).floor.toInt
//    )
    val cell = Cell(
      (obj.x / tilemap.tileWidth).floor.toInt,
      tilemap.height - (obj.y / tilemap.tileHeight).floor.toInt - 1)
    val properties = obj.properties.asScala
    println("Spawning " + obj.`type`)
    (obj.`type` match {
      case "Player" => new Player(multiverse, cell)
      case "Quball" => new Quball(multiverse, cell)
      case "Laser" =>
        val gate = Gate.X // Gate.withName(properties("Gate").value)
        val direction = Direction.withName(properties("Direction").value)
        val control = properties.get("Control").flatMap(c => cellFromString(tilemap, c.value)).toList
        new Laser(multiverse, cell, gate, direction, control)
      case "Door" =>
        val controls = cellsFromString(tilemap, properties("Controls").value).toList
        new Door(multiverse, cell, controls)
      case "Goal" =>
        // val requires = ObjectId(properties("Requires").value.toInt)
        lazy val requires = Player.All.head.cell
        val next = properties("Next Level").value
        new Goal(multiverse, cell, requires, () => load(Tilemap.load(getClass.getResource(next))))
      case _ => null
    }, id)
  }

  private def cellFromString(tilemap: Tilemap, string: String): Option[Cell] =
    """\((\d+),\s*(\d+)\)"""
      .r("column", "row")
      .findFirstMatchIn(string)
      .map(m => Cell(m.group("column").trim.toInt, tilemap.height - m.group("row").trim.toInt - 1))

  private def cellsFromString(tilemap: Tilemap, string: String): Seq[Cell] =
    string.linesIterator.flatMap(cellFromString(tilemap, _)).toSeq

  private def wallsInTilemap(tilemap: Tilemap): Set[Cell] =
    (for (layer <- tilemap.layers.asScala if layer.properties.asScala.get("Collision").exists(_.value.toBoolean);
          x <- 0 until layer.width;
          y <- 0 until layer.height if layer.data.tiles(x)(y) != 0) yield {
      Cell(
        (x + layer.offsetX.toDouble / tilemap.tileWidth).round.toInt,
        (y + layer.offsetY.toDouble / tilemap.tileHeight).round.toInt
      )
    }).toSet
}
