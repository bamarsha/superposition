package superposition

import engine.core.Behavior.Entity
import engine.core.Game.dt
import engine.core.Input
import engine.graphics.sprites.Sprite
import engine.util.Color.WHITE
import engine.util.math.{Transformation, Vec2d}
import org.lwjgl.glfw.GLFW.{GLFW_KEY_A, GLFW_KEY_D, GLFW_KEY_S, GLFW_KEY_W}

private object Player {
  val speed: Double = 5
}

private class Player extends Entity {
  import Player._

  var position: Vec2d = new Vec2d(0, 0)

  def step(): Unit = {
    if (Input.keyDown(GLFW_KEY_W)) {
      position = position.add(new Vec2d(0, speed * dt()))
    }
    if (Input.keyDown(GLFW_KEY_A)) {
      position = position.add(new Vec2d(-speed * dt(), 0))
    }
    if (Input.keyDown(GLFW_KEY_S)) {
      position = position.add(new Vec2d(0, -speed * dt()))
    }
    if (Input.keyDown(GLFW_KEY_D)) {
      position = position.add(new Vec2d(speed * dt(), 0))
    }
    draw()
  }

  private def draw(): Unit = {
    Sprite.load("cat.png").draw(Transformation.create(position, 0, 1), WHITE)
  }
}
