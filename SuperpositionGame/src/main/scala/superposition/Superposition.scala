package superposition

import engine.core.{Game, Settings}
import extras.behaviors.{FPSBehavior, QuitOnEscapeBehavior}
import extras.tiles.Tilemap

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

    Game.init()
    Multiverse.declareSystem()
    Laser.declareSystem()
    Door.declareSystem()
    Player.declareSystem()
    Level.declareSystem()
    Goal.declareSystem()

    Game.create(new FPSBehavior())
    Game.create(new QuitOnEscapeBehavior())

    Level.load(Tilemap.load(getClass.getResource("level1.tmx")))
    Game.run()
  }
}
