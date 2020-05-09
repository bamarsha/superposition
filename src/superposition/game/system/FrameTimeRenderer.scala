package superposition.game.system

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.g2d.{BitmapFont, SpriteBatch}

/** Displays the time it takes to render each frame.
  *
  * @param frameNanoTime a function that returns the time it took to render the last frame in nanoseconds
  */
final class FrameTimeRenderer(frameNanoTime: () => Long) extends EntitySystem {
  /** The sprite batch. */
  private val batch: SpriteBatch = new SpriteBatch

  /** The font. */
  private val font: BitmapFont = new BitmapFont

  override def update(deltaTime: Float): Unit = {
    val ms = (frameNanoTime() / 1e6d).round
    batch.begin()
    font.draw(batch, s"$ms ms", 8, 20)
    batch.end()
  }
}
