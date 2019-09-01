package superposition

import engine.core.{Game, Settings}
import engine.graphics.Camera
import engine.util.Color
import engine.util.math.Vec2d
import extras.behaviors.{FPSBehavior, QuitOnEscapeBehavior}
import superposition.Levels.level1

/**
 * The main class for the Superposition game.
 */
object Superposition {
  /**
   * Runs the Superposition game.
   *
   * @param args the command-line arguments
   */
  def main(args: Array[String]): Unit = {
    Settings.WINDOW_WIDTH = 1280
    Settings.WINDOW_HEIGHT = 720
    Settings.BACKGROUND_COLOR = Color.GRAY

    Game.init()
    Game.declareSystem(classOf[Multiverse], (_: Multiverse).step())
    Game.declareSystem(classOf[Player], (_: Player).step())

    Camera.camera2d.setCenterSize(new Vec2d(0, 0), new Vec2d(16, 9))

    Game.create(new FPSBehavior())
    Game.create(new QuitOnEscapeBehavior())
    Game.create(level1())

    Game.run()
  }
}
