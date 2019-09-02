package superposition

import engine.core.Game
import engine.graphics.sprites.Sprite
import engine.util.math.Vec2d
import extras.physics.Rectangle

/**
 * Stores game levels.
 */
private object Levels {
  /**
   * Loads level 1.
   *
   * @return the multiverse for level 1
   */
  def level1(): Multiverse = {
    val sprite = Sprite.load(getClass.getResource("sprites/cat.png"))
    val walls = List(
      new Wall(sprite, new Rectangle(new Vec2d(-16, -9), new Vec2d(-15, 9))),
      new Wall(sprite, new Rectangle(new Vec2d(-16, -9), new Vec2d(16, -8))),
      new Wall(sprite, new Rectangle(new Vec2d(4, -0.5), new Vec2d(8, 0.5))),
      new Wall(sprite, new Rectangle(new Vec2d(-8, -0.5), new Vec2d(-4, 0.5))),
      new Wall(sprite, new Rectangle(new Vec2d(-0.5, 2), new Vec2d(0.5, 6.5))),
      new Wall(sprite, new Rectangle(new Vec2d(-0.5, -6.5), new Vec2d(0.5, -2))),
      new Wall(sprite, new Rectangle(new Vec2d(-16, 8), new Vec2d(16, 9))),
      new Wall(sprite, new Rectangle(new Vec2d(15, -9), new Vec2d(16, 9)))
    )

    lazy val multiverse: Multiverse = new Multiverse(List(universe), walls)
    lazy val universe = new Universe(multiverse)
    for (i <- 0 until 2) {
      universe.add(new Quball(universe, UniversalId(i), new Vec2d(1 + i, 1)))
    }
    universe.add(new Player(universe, UniversalId(2), new Vec2d(0, 0)))
    multiverse
  }
}
