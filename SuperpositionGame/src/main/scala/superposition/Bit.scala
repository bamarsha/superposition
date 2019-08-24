package superposition

import engine.core.Game.dt
import engine.core.Input
import engine.graphics.sprites.Sprite
import engine.util.Color.{BLACK, WHITE}
import engine.util.math.{Transformation, Vec2d}

/**
 * A physical object that carries a single bit as its state.
 *
 * Bits have a position and velocity in addition to their state and can be dragged using the mouse.
 *
 * @param position the initial position
 * @param velocity the initial velocity
 * @param on the initial state
 */
private case class Bit(var position: Vec2d = new Vec2d(0.0, 0.0),
                       var velocity: Vec2d = new Vec2d(0.0, 0.0),
                       var on: Boolean = false) {
  private var selected: Boolean = false

  /**
   * Steps physics forward for this bit.
   */
  def step(): Unit = {
    position = position.add(velocity.mul(dt()))
    selected = Input.mouseDown(0) && (selected || Input.mouse().sub(position).length() < 0.5)
    if (selected) {
      position = position.lerp(Input.mouse(), dt())
    }
  }

  /**
   * Draws this bit.
   */
  def draw(): Unit = {
    val color = if (on) WHITE else BLACK
    Sprite.load("cat.png").draw(Transformation.create(position, 0, 1), color)
  }
}
