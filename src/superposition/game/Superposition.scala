package superposition.game

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx.graphics
import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import superposition.game.Superposition.playlist
import superposition.graphics._
import superposition.system._

/** The Superposition game. */
private final class Superposition extends Game {
  /** The entity engine. */
  private val engine: Engine = new Engine

  /** The time it took to render the last frame in nanoseconds. */
  private var renderNanoTime: Long = 0

  override def create(): Unit = {
    val levels = new LevelPlaylist(engine)
    levels.appendAll(new TmxMapLoader(ResourceResolver), playlist)
    levels.play()

    engine.addSystem(new LevelSystem(levels))
    engine.addSystem(new PlayerInputSystem(() => levels.current))
    engine.addSystem(new LaserInputSystem(() => levels.current))
    engine.addSystem(new AnimationSystem(() => levels.current))
    engine.addSystem(new RenderingSystem(Iterable(
      new MapLayerRenderer(() => levels.current),
      new SpriteRenderer(() => levels.current),
      new BeamRenderer(() => levels.current),
      new CellHighlightRenderer(() => levels.current),
      new MultiverseRenderer)))
    engine.addSystem(new StateDisplaySystem(() => levels.current))
    engine.addSystem(new DebugDisplaySystem(() => renderNanoTime))
  }

  override def render(): Unit = {
    val startTime = System.nanoTime()
    engine.update(graphics.getDeltaTime)
    super.render()
    renderNanoTime = System.nanoTime() - startTime
  }
}

/** The main class for Superposition. */
private object Superposition {
  /** The level playlist. */
  private val playlist: Seq[String] = Seq(
    "level_base.tmx",
    "level_x1.tmx",
    "level_x2.tmx",
    "level_super1.tmx",
    "level_super2.tmx",
    "level_super3.tmx",
    "level_reg1.tmx",
    "level_reg2.tmx",
    "level_swap.tmx",
    "level_win.tmx")

  /** The entry point for Superposition.
    *
    * @param args the command-line arguments
    */
  def main(args: Array[String]): Unit = {
    val config = new Lwjgl3ApplicationConfiguration
    config.setTitle("Superposition")
    config.setWindowedMode(1536, 864)
    config.useOpenGL3(true, 3, 3)
    new Lwjgl3Application(new Superposition, config)
  }
}
