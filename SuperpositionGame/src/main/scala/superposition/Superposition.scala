package superposition

import engine.core.{Game, Settings}
import engine.graphics.Camera
import engine.util.Color
import engine.util.math.Vec2d
import extras.behaviors.{FPSBehavior, QuitOnEscapeBehavior}

object Superposition {
  def main(args: Array[String]): Unit = {
    Settings.WINDOW_WIDTH = 1280
    Settings.WINDOW_HEIGHT = 720
    Settings.BACKGROUND_COLOR = Color.GRAY
    Settings.ENABLE_VSYNC = false

    Game.init()
    Game.declareSystem(classOf[Multiverse], (_: Multiverse).step())
    Game.declareSystem(classOf[Player], (_: Player).step())

    Camera.camera2d.setCenterSize(new Vec2d(0, 0), new Vec2d(16, 9))

    new FPSBehavior().create()
    new QuitOnEscapeBehavior().create()
    new Multiverse().create()
    new Player().create()

    Game.run()
  }
}
