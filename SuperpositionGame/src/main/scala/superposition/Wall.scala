package superposition

import engine.graphics.sprites.Sprite
import engine.util.Color.WHITE
import engine.util.math.{Transformation, Vec2d}
import extras.physics.Rectangle

/**
 * A wall in the environment.
 *
 * @param hitbox the hitbox for this wall
 * @param sprite the sprite for this wall
 */
private final class Wall(val hitbox: Rectangle, sprite: Sprite) {
  /**
   * Draws this wall.
   */
  def draw(): Unit = {
    val span = hitbox.upperRight.sub(hitbox.lowerLeft)
    for (x <- 0 until span.x.asInstanceOf[Int];
         y <- 0 until span.y.asInstanceOf[Int]) {
      val center = hitbox.lowerLeft.add(new Vec2d(x + 0.5, y + 0.5))
      sprite.draw(Transformation.create(center, 0, 1), WHITE)
    }
  }
}
