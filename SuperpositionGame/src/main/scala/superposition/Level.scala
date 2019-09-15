package superposition

import engine.core.{Game, Input}
import engine.graphics.Camera
import extras.tiles.Tilemap
import org.lwjgl.glfw.GLFW.GLFW_KEY_R

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
    lazy val multiverse: Multiverse = new Multiverse(universe, Tilemap.load(getClass.getResource("level3.tmx")))
    lazy val universe = new Universe(multiverse)
    universe.add(new Player(universe, UniversalId(0), Cell(4, 11)))
    universe.add(new Quball(universe, UniversalId(1), Cell(4, 16)))
    universe.add(new Laser(universe, UniversalId(2), Cell(4, 20), Gate.X, Direction.Up, Some(Cell(3, 20))))
    universe.add(new Laser(universe, UniversalId(3), Cell(7, 23), Gate.X, Direction.Left, None))
    universe.add(new Door(universe, UniversalId(4), Cell(7, 4), Seq(Cell(5, 4))))
    universe.add(new Door(universe, UniversalId(5), Cell(9, 19), Seq(Cell(7, 19))))
    universe.add(new Quball(universe, UniversalId(6), Cell(11, 4)))
    universe.add(new Goal(universe, UniversalId(7), Cell(11, 19), UniversalId(0), () => load(level1())))
    multiverse.applyGate(Gate.H, UniversalId(1), None)
    multiverse
  }
}
