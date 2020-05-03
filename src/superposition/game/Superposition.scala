package superposition.game

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx.graphics
import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import superposition.game.Superposition.Playlist
import superposition.game.system._

private final class Superposition extends Game {
  private val engine: Engine = new Engine

  private val levelLoader: LevelLoader = new LevelLoader(engine)

  override def create(): Unit = {
    engine.addSystem(new LevelControl(levelLoader))
    engine.addSystem(new PlayerControl)
    engine.addSystem(new LaserControl)
    engine.addSystem(new MapRenderer)
    engine.addSystem(new MultiverseRenderer)
    levelLoader.startPlaylist(new TmxMapLoader(ResourceResolver), Playlist)
  }

  override def render(): Unit = {
    engine.update(graphics.getDeltaTime)
    super.render()
  }
}

private object Superposition {
  private val Playlist: Seq[String] = Seq(
    "level1.tmx",
    "level2.tmx",
    "level3.tmx",
    "level4.tmx",
    "win.tmx")

  def main(args: Array[String]): Unit = {
    val config = new Lwjgl3ApplicationConfiguration
    config.setTitle("Superposition")
    config.setWindowedMode(1280, 720)
    new Lwjgl3Application(new Superposition, config)
  }
}
