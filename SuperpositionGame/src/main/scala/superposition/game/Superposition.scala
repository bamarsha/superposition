package superposition.game

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx.graphics
import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import com.badlogic.gdx.maps.tiled.TmxMapLoader

private final class Superposition extends Game {
  private val engine: Engine = new Engine

  private val levelManager: LevelManager = new LevelManager(engine)

  override def create(): Unit = {
    engine.addSystem(new MapRenderer)
    engine.addSystem(new MultiverseRenderer)
    levelManager.load(new TmxMapLoader(ResourceResolver).load("level1.tmx"))
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

//    Game.init()
//    Goal.declareSystem()
//    Laser.declareSystem()
//    Level.declareSystem()
//    Multiverse.declareSystem()
//    Player.declareSystem()
//
//    Game.create(new FPSBehavior())
//    Game.create(new QuitOnEscapeBehavior())
//
//    Level.load(Tilemap.load(getClass.getResource("level1.tmx")))
//    Game.run()
