package superposition.game

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx.graphics
import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import com.badlogic.gdx.maps.tiled.TmxMapLoader
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
    levelLoader.load(new TmxMapLoader(ResourceResolver).load("level1.tmx"))
  }

  override def render(): Unit = {
    engine.update(graphics.getDeltaTime)
    super.render()
  }
}

private object Superposition extends App {
  val config = new Lwjgl3ApplicationConfiguration
  config.setTitle("Superposition")
  config.setWindowedMode(1280, 720)
  new Lwjgl3Application(new Superposition, config)
}
