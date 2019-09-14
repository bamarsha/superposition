package superposition

import engine.core.{Game, Input}
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
    multiverse foreach Game.destroy
    supplier = () => Some(level)
    val current = level
    Game.create(current)
    multiverse = Some(current)
  }

  /**
   * A new instance of level 1: X.
   */
  def level1(): Multiverse = {
    lazy val multiverse: Multiverse = new Multiverse(universe, Tilemap.load(getClass.getResource("level3.tmx")))
    lazy val universe = new Universe(multiverse)
    universe.add(new Player(universe, UniversalId(0), Cell(-5, -5)))
    universe.add(new Quball(universe, UniversalId(1), Cell(-5, 0)))
    universe.add(new Laser(universe, UniversalId(3), Cell(-2, 7), Gate.X, Direction.Left, None))
    universe.add(new Door(universe, UniversalId(5), Cell(0, 3), Seq(Cell(-2, 3))))
    universe.add(new Goal(universe, UniversalId(7), Cell(2, 3), UniversalId(0), () => load(level2())))
    multiverse
  }

  /**
   * A new instance of level 2: CNOT.
   */
  def level2(): Multiverse = {
    lazy val multiverse: Multiverse = new Multiverse(universe, Tilemap.load(getClass.getResource("level2.tmx")))
    lazy val universe = new Universe(multiverse)
    universe.add(new Player(universe, UniversalId(0), Cell(-5, -5)))
    universe.add(new Quball(universe, UniversalId(1), Cell(-5, 0)))
    universe.add(new Quball(universe, UniversalId(2), Cell(-5, -1)))
    universe.add(new Laser(universe, UniversalId(3), Cell(-5, 4), Gate.X, Direction.Up, Some(Cell(-6, 4))))
    universe.add(new Door(universe, UniversalId(4), Cell(0, 3), Seq(Cell(-2, 2), Cell(-2, 4))))
    universe.add(new Goal(universe, UniversalId(5), Cell(2, 3), UniversalId(0), () => load(level3())))
    multiverse.applyGate(Gate.X, UniversalId(1), None)
    multiverse
  }

  /**
   * A new instance of level 3: SWAP.
   */
  def level3(): Multiverse = {
    lazy val multiverse: Multiverse = new Multiverse(universe, Tilemap.load(getClass.getResource("level3.tmx")))
    lazy val universe = new Universe(multiverse)
    universe.add(new Player(universe, UniversalId(0), Cell(-5, -5)))
    universe.add(new Quball(universe, UniversalId(1), Cell(-5, 0)))
    universe.add(new Laser(universe, UniversalId(2), Cell(-5, 4), Gate.X, Direction.Up, Some(Cell(-6, 4))))
    universe.add(new Laser(universe, UniversalId(3), Cell(-2, 7), Gate.X, Direction.Left, None))
    universe.add(new Door(universe, UniversalId(4), Cell(-2, -12), Seq(Cell(-4, -12))))
    universe.add(new Door(universe, UniversalId(5), Cell(0, 3), Seq(Cell(-2, 3))))
    universe.add(new Quball(universe, UniversalId(6), Cell(2, -12)))
    universe.add(new Goal(universe, UniversalId(7), Cell(2, 3), UniversalId(0), () => load(level1())))
    multiverse.applyGate(Gate.H, UniversalId(1), None)
    multiverse
  }
}
