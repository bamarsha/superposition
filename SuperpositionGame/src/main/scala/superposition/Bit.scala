package superposition

import engine.core.Game.dt
import engine.core.Input
import engine.util.math.Vec2d

private case class Bit(var position: Vec2d = new Vec2d(0.0, 0.0),
                       var velocity: Vec2d = new Vec2d(0.0, 0.0),
                       var on: Boolean = false) {
  private var selected: Boolean = false

  def step(): Unit = {
    position = position.add(velocity.mul(dt()))
    selected = Input.mouseDown(0) && (selected || Input.mouse().sub(position).length() < 0.5)
    if (selected) {
      position = position.lerp(Input.mouse(), dt())
    }
  }
}
