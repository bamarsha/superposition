package superposition

import engine.core.Game
import extras.tiles.Tilemap

/**
 * Stores game levels.
 */
private object Levels {
  /**
   * Creates level 1.
   */
  def createLevel1(): Unit = {
    lazy val multiverse: Multiverse = new Multiverse(List(universe), Tilemap.load(getClass.getResource("level1.tmx")))
    lazy val universe = new Universe(multiverse)
    universe.add(new Player(universe, UniversalId(0), Cell(-5, -5)))
    universe.add(new Quball(universe, UniversalId(1), Cell(-5, 0)))
    universe.add(new Laser(universe, UniversalId(2), Cell(-5, 4), Gate.X, Direction.Up, Some(Cell(-6, 4))))
    universe.add(new Laser(universe, UniversalId(3), Cell(-2, 7), Gate.X, Direction.Left, None))
    universe.add(new Door(universe, UniversalId(4), Cell(-2, -12), Cell(-4, -12)))
    Game.create(multiverse)
  }
}
