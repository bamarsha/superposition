package superposition.system

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx.graphics
import com.badlogic.gdx.graphics.g2d.{BitmapFont, SpriteBatch}

/** Displays information about the game to help with debugging.
  *
  * @param renderNanoTime a function that returns the time it took to render the last frame in nanoseconds
  */
final class DebugInfoSystem(renderNanoTime: () => Long) extends EntitySystem {
  /** The sprite batch. */
  private val batch: SpriteBatch = new SpriteBatch

  /** The font. */
  private val font: BitmapFont = new BitmapFont

  override def update(deltaTime: Float): Unit = {
    val fps = graphics.getFramesPerSecond
    val ms = (renderNanoTime() / 1e6d).round
    batch.begin()
    font.draw(batch, s"$fps fps", 8, 20)
    font.draw(batch, s"$ms ms", 60, 20)
    batch.end()
  }
}
