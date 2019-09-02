package superposition

import engine.graphics.sprites.Sprite
import engine.util.Color.WHITE
import engine.util.math.{Transformation, Vec2d}
import extras.physics.Rectangle

/**
 * A wall in the environment.
 *
 * @param rectangle the bounding rectangle for this wall
 * @param sprite    the sprite for this wall
 */
private final class Wall(val rectangle: Rectangle, sprite: Sprite) {
  /**
   * Draws this wall.
   */
  def draw(): Unit = {
    val span = rectangle.upperRight.sub(rectangle.lowerLeft)
    for (x <- 0 until span.x.asInstanceOf[Int];
         y <- 0 until span.y.asInstanceOf[Int]) {
      val center = rectangle.lowerLeft.add(new Vec2d(x + 0.5, y + 0.5))
      sprite.draw(Transformation.create(center, 0, 1), WHITE)
    }
  }
}
