package superposition

import engine.core.Behavior.Entity
import engine.core.{Game, Input}
import engine.graphics.Camera
import extras.tiles.Tilemap
import org.lwjgl.glfw.GLFW.GLFW_KEY_R

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
   * Loads the level.
   *
   * @param level the level to load
   */
  def load(level: => Multiverse): Unit = {
    this.multiverse foreach Game.destroy
    supplier = () => Some(level)

    val multiverse = level
    Camera.camera2d.lowerLeft = multiverse.boundingBox.lowerLeft
    Camera.camera2d.upperRight = multiverse.boundingBox.upperRight
    Game.create(multiverse)

    this.multiverse = Some(multiverse)
  }

  /**
   * A new instance of level 1: X.
   */
  def level1(): Multiverse = {
    lazy val multiverse: Multiverse = new Multiverse(universe, Tilemap.load(getClass.getResource("level3.tmx")))
    lazy val universe = new Universe(multiverse)
    universe.add(new Player(universe, UniversalId(0), Cell(4, 11)))
    universe.add(new Quball(universe, UniversalId(1), Cell(4, 16)))
    universe.add(new Laser(universe, UniversalId(2), Cell(7, 23), Gate.X, Direction.Left, None))
    universe.add(new Door(universe, UniversalId(3), Cell(9, 19), Seq(Cell(7, 19))))
    universe.add(new Goal(universe, UniversalId(4), Cell(11, 19), UniversalId(0), () => load(level2())))
    multiverse
  }

  /**
   * A new instance of level 2: CNOT.
   */
  def level2(): Multiverse = {
    lazy val multiverse: Multiverse = new Multiverse(universe, Tilemap.load(getClass.getResource("level2.tmx")))
    lazy val universe = new Universe(multiverse)
    universe.add(new Player(universe, UniversalId(0), Cell(4, 11)))
    universe.add(new Quball(universe, UniversalId(1), Cell(4, 16)))
    universe.add(new Quball(universe, UniversalId(2), Cell(4, 15)))
    universe.add(new Laser(universe, UniversalId(3), Cell(4, 20), Gate.X, Direction.Up, Some(Cell(3, 20))))
    universe.add(new Door(universe, UniversalId(4), Cell(9, 19), Seq(Cell(7, 18), Cell(7, 20))))
    universe.add(new Goal(universe, UniversalId(5), Cell(11, 19), UniversalId(0), () => load(level3())))
    multiverse.applyGate(Gate.X, UniversalId(1), None)
    multiverse
  }

  /**
   * A new instance of level 3: SWAP.
   */
  def level3(): Multiverse = {
    val tilemap = Tilemap.load(getClass.getResource("level3.tmx"))
    lazy val multiverse: Multiverse = new Multiverse(universe, tilemap)
    lazy val universe = new Universe(multiverse)
    for (group <- tilemap.objectGroups.asScala; obj <- group.objects.asScala) {
      universe.add(entityFromObject(tilemap, obj, universe))
    }
    // TODO: Add the goal to the TMX file.
    universe.add(new Goal(universe, UniversalId(0), Cell(11, 19), UniversalId(5), () => load(level1())))
    multiverse.applyGate(Gate.H, UniversalId(5), None)
    multiverse
  }

  private def entityFromObject(tilemap: Tilemap, obj: Tilemap#ObjectGroup#Object, universe: Universe): Entity = {
    val id = UniversalId(obj.id)
    val cell = Cell(
      tilemap.height - (obj.y / tilemap.tileHeight).floor.toInt - 1,
      (obj.x / tilemap.tileWidth).floor.toInt
    )
    val properties = obj.properties.asScala
    obj.`type` match {
      case "Player" => new Player(universe, id, cell)
      case "Quball" => new Quball(universe, id, cell)
      case "Laser" =>
        val gate = Gate.withName(properties("Gate").value)
        val direction = Direction.withName(properties("Direction").value)
        val control = properties.get("Control").map(p => cellFromString(tilemap, p.value))
        new Laser(universe, id, cell, gate, direction, control)
      case "Door" =>
        val control = cellFromString(tilemap, properties("Control").value)
        new Door(universe, id, cell, Seq(control))
    }
  }

  private def cellFromString(tilemap: Tilemap, s: String): Cell = {
    val Array(column, row) = s.split(',')
    Cell(tilemap.height - row.trim.toInt - 1, column.trim.toInt)
  }
}
