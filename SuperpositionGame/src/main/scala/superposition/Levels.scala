package superposition

import engine.core.Game
import engine.util.math.Vec2d

/**
 * Stores game levels.
 */
private object Levels {
  /**
   * Creates level 1.
   *
   * @return the universe for level 1
   */
  def level1(): Universe = {
    val universe = new Universe()
    Game.create(universe)
    for (i <- 0 until 2) {
      Game.create(new Quball(universe, UniversalId(i), new Vec2d(1 + i, 1)))
    }
    Game.create(new Player(universe, UniversalId(2), new Vec2d(0, 0)))
    universe
  }
}
