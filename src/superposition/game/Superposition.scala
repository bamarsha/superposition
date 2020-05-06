package superposition.game

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx.graphics
import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import superposition.game.Superposition.Playlist
import superposition.game.system._

/** The Superposition game. */
private final class Superposition extends Game {
  /** The entity engine. */
  private val engine: Engine = new Engine

  override def create(): Unit = {
    val levelLoader = new LevelLoader(engine)
    engine.addSystem(new LevelSystem(levelLoader))
    engine.addSystem(new MapRenderer)
    engine.addSystem(new MultiverseRenderer)
    engine.addSystem(new PlayerInputSystem)
    engine.addSystem(new LaserSystem)
    engine.addSystem(new SpriteRenderer)
    levelLoader.startPlaylist(new TmxMapLoader(ResourceResolver), Playlist)
  }

  override def render(): Unit = {
    engine.update(graphics.getDeltaTime)
    super.render()
  }
}

/** The main class for Superposition. */
private object Superposition {
  /** The level playlist. */
  private val Playlist: Seq[String] = Seq(
    "level1.tmx",
    "level2.tmx",
    "level3.tmx",
    "level4.tmx",
    "win.tmx")

  /** The entry point for Superposition.
    *
    * @param args the command-line arguments
    */
  def main(args: Array[String]): Unit = {
    val config = new Lwjgl3ApplicationConfiguration
    config.setTitle("Superposition")
    config.setWindowedMode(1280, 720)
    new Lwjgl3Application(new Superposition, config)
  }
}
