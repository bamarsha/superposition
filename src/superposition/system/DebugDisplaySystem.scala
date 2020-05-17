package superposition.system

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx.graphics
import com.badlogic.gdx.graphics.g2d.{Batch, BitmapFont, SpriteBatch}
import com.badlogic.gdx.utils.Disposable

/** Displays debugging information about the game.
  *
  * @param renderNanoTime a function that returns the time it took to render the last frame in nanoseconds
  */
final class DebugDisplaySystem(renderNanoTime: () => Long) extends EntitySystem with Disposable {
  /** The batch. */
  private val batch: Batch = new SpriteBatch

  /** The display font. */
  private val font: BitmapFont = new BitmapFont

  override def update(deltaTime: Float): Unit = {
    val fps = graphics.getFramesPerSecond
    val ms = (renderNanoTime() / 1e6d).round
    batch.begin()
    font.draw(batch, s"$fps fps", 8, 20)
    font.draw(batch, s"$ms ms", 60, 20)
    batch.end()
  }

  override def dispose(): Unit = {
    batch.dispose()
    font.dispose()
  }
}
