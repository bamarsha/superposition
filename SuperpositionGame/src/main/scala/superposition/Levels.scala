package superposition

import engine.core.Game
import engine.util.math.Vec2d
import extras.physics.Rectangle

/**
 * Stores game levels.
 */
private object Levels {
  /**
   * Creates level 1.
   *
   * @return the multiverse for level 1
   */
  def level1(): Multiverse = {
    val walls = List(
      new Rectangle(new Vec2d(-8, -4.5), new Vec2d(-8, 4.5)),
      new Rectangle(new Vec2d(-8, -4.5), new Vec2d(8, -4.5)),
      new Rectangle(new Vec2d(-8, 4.5), new Vec2d(8, 4.5)),
      new Rectangle(new Vec2d(8, -4.5), new Vec2d(8, 4.5))
    )
    lazy val multiverse: Multiverse = new Multiverse(List(universe), walls)

    lazy val universe = new Universe(multiverse)
    Game.create(universe)
    for (i <- 0 until 2) {
      Game.create(new Quball(universe, UniversalId(i), new Vec2d(1 + i, 1)))
    }
    Game.create(new Player(universe, UniversalId(2), new Vec2d(0, 0)))

    multiverse
  }
}
