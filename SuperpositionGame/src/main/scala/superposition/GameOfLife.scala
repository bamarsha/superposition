package superposition

import extras.behaviors.FPSBehavior
import extras.behaviors.QuitOnEscapeBehavior
import engine.core.Game
import engine.core.Game.dt
import engine.core.Input
import engine.core.Settings
import engine.graphics.Camera
import engine.graphics.Graphics
import engine.graphics.Window
import org.lwjgl.glfw.GLFW.GLFW_KEY_A
import org.lwjgl.glfw.GLFW.GLFW_KEY_D
import org.lwjgl.glfw.GLFW.GLFW_KEY_H
import org.lwjgl.glfw.GLFW.GLFW_KEY_R
import org.lwjgl.glfw.GLFW.GLFW_KEY_S
import org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE
import org.lwjgl.glfw.GLFW.GLFW_KEY_W
import engine.util.Color
import engine.util.math.MathUtils.ceil
import engine.util.math.MathUtils.clamp
import engine.util.math.MathUtils.floor
import engine.util.math.MathUtils.mod
import engine.util.math.Transformation
import engine.util.math.Vec2d

object GameOfLife {
  private val SIZE = 1000
  private var STATE = Array.ofDim[Boolean](SIZE, SIZE)

  private var viewPos = new Vec2d(0, 0)
  private var viewZoom = 0.0
  private var viewSize = new Vec2d(16, 9)
  private var running = false

  def main(args: Array[String]): Unit = {
    Game.init()

    new FPSBehavior().create()
    new QuitOnEscapeBehavior().create()

    Game.declareSystem(() => {
      var dx = 0
      var dy = 0

      if (Input.keyDown(GLFW_KEY_W)) {
        dy += 1
      }
      if (Input.keyDown(GLFW_KEY_A)) {
        dx -= 1
      }
      if (Input.keyDown(GLFW_KEY_S)) {
        dy -= 1
      }
      if (Input.keyDown(GLFW_KEY_D)) {
        dx += 1
      }
      viewPos = viewPos.add(new Vec2d(dx, dy).mul(5 * Math.pow(2, -0.5 * viewZoom) * dt))

      viewZoom += Input.mouseWheel
      viewZoom = clamp(viewZoom, -14, 0)

      if (Input.mouseDown(0)) {
        set(floor(Input.mouse.x), floor(Input.mouse.y), value = true)
      }
      if (Input.mouseDown(1)) {
        set(floor(Input.mouse.x), floor(Input.mouse.y), value = false)
      }
      if (Input.keyJustPressed(GLFW_KEY_SPACE)) {
        running = !running
      }
      if (Input.keyJustPressed(GLFW_KEY_R)) {
        for (x <- 0 until SIZE; y <- 0 until SIZE) {
          if (Math.random() < 0.01) {
            set(x, y, !get(x, y))
          }
        }
      }

      if (running) {
        STATE = nextState
      }
      if (Input.keyJustPressed(GLFW_KEY_H)) {
        Window.resizeWindow(400, 400)
      }

      viewSize = new Vec2d(16, 16.0 * Settings.WINDOW_HEIGHT / Settings.WINDOW_WIDTH)
      Camera.camera2d.setCenterSize(viewPos, viewSize.mul(Math.pow(2, -.5 * viewZoom)))
      for (x <- floor(Camera.camera2d.lowerLeft.x) until ceil(Camera.camera2d.upperRight.x);
           y <- floor(Camera.camera2d.lowerLeft.y) until ceil(Camera.camera2d.upperRight.y)) {
        if (get(x, y)) {
          Graphics.drawRectangle(Transformation.create(new Vec2d(x, y), 0, 1), Color.WHITE);
        }
      }
    })

    Game.run()
  }

  private def nextState: Array[Array[Boolean]] =
    Array.tabulate[Boolean](SIZE, SIZE)((x, y) => {
      var neighborCount = 0
      var i = -1
      while (i <= 1) {
        var j = -1
        while (j <= 1) {
          neighborCount += (if (get(x + i, y + j)) 1 else 0)
          j += 1
        }
        i += 1
      }
      neighborCount == 3 || neighborCount == 4 && get(x, y)
    })

  private def get(x: Int, y: Int): Boolean = STATE(mod(x, SIZE))(mod(y, SIZE))

  private def set(x: Int, y: Int, value: Boolean): Unit = {
    STATE(mod(x, SIZE))(mod(y, SIZE)) = value
  }
}
