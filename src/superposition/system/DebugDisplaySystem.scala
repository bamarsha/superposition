package superposition.system

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx.graphics
import com.badlogic.gdx.graphics.g2d.{Batch, BitmapFont, SpriteBatch}
import com.badlogic.gdx.utils.Disposable
import superposition.system.DebugDisplaySystem.CircularBuffer

import scala.reflect.ClassTag

/** Displays debugging information about the game.
  *
  * @param renderNanoTime a function that returns the time it took to render the last frame in nanoseconds
  */
final class DebugDisplaySystem(renderNanoTime: () => Long) extends EntitySystem with Disposable {

  /** The batch. */
  private val batch: Batch = new SpriteBatch

  /** The display font. */
  private val font: BitmapFont = new BitmapFont

  /** A sliding window of render times from the last several frames. */
  private val renderTimes: CircularBuffer[Long] = new CircularBuffer(500)

  override def update(deltaTime: Float): Unit = {
    val fps = graphics.getFramesPerSecond
    val usedMemory = (Runtime.getRuntime.totalMemory - Runtime.getRuntime.freeMemory) / (2 << 19)

    renderTimes.push((renderNanoTime() / 1e6d).round)
    val lastRenderTime = renderTimes.head
    val averageRenderTime = renderTimes.fold(0L)(_ + _) / renderTimes.length
    val maxRenderTime = renderTimes.fold(Long.MinValue)(math.max)

    batch.begin()
    font.draw(batch, s"$fps FPS", 8, 20)
    font.draw(batch, s"$lastRenderTime/$averageRenderTime/$maxRenderTime ms", 80, 20)
    font.draw(batch, s"$usedMemory MB", 170, 20)
    batch.end()
  }

  override def dispose(): Unit = {
    batch.dispose()
    font.dispose()
  }
}

private object DebugDisplaySystem {

  /** A buffer with a fixed capacity that drops the least recently added element when its capacity is exceeded.
    *
    * The elements are in order from most to least recently added.
    *
    * @param capacity the maximum length of the buffer
    * @tparam A the type of the buffer elements
    */
  private final class CircularBuffer[@specialized(Long) A: ClassTag](capacity: Int) {

    /** The backing array. */
    private val array: Array[A] = Array.ofDim(capacity)

    /** The index of the most recently added element. */
    private var index: Int = 0

    /** The length of the buffer. */
    private var _length: Int = 0

    /** Returns the element at index `i`.
      *
      * @param i the index
      * @return the element at index `i`
      */
    def apply(i: Int): A = {
      require(0 <= i && i < _length)
      if (index - i < 0)
        array(index - i + capacity)
      else array(index - i)
    }

    /** The length of the buffer. */
    def length: Int = _length

    /** The first element in the buffer. */
    def head: A = this(0)

    /** Pushes an element into the beginning of the buffer.
      *
      * @param value the element value
      */
    def push(value: A): Unit = {
      index = (index + 1) % capacity
      array(index) = value
      _length = math.min(_length + 1, capacity)
    }

    /** Folds the buffer from left to right.
      *
      * @param init the initial value of the accumulator
      * @param f the folding function which is given the accumulator and the current element
      * @return the accumulated value
      */
    def fold(init: A)(f: (A, A) => A): A = {
      var acc = init
      var index = 0
      while (index < length) {
        acc = f(acc, this(index))
        index += 1
      }
      acc
    }
  }
}
