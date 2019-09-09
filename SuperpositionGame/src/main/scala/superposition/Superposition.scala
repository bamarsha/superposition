package superposition

import engine.core.{Game, Settings}
import engine.graphics.Camera
import engine.util.Color
import engine.util.math.Vec2d
import extras.behaviors.{FPSBehavior, QuitOnEscapeBehavior}
import superposition.Levels.createLevel1

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
    Multiverse.declareSystem()
    Laser.declareSystem()
    Door.declareSystem()
    Player.declareSystem()
    Draw.declareSystem()

    Camera.camera2d.setCenterSize(new Vec2d(0, 0), new Vec2d(32, 18))

    Game.create(new FPSBehavior())
    Game.create(new QuitOnEscapeBehavior())

    createLevel1()
    Game.run()
  }
}
