package superposition

import engine.graphics.Camera
import engine.graphics.Camera.Camera2d
import engine.graphics.opengl.Shader
import engine.util.math.{Transformation, Vec2d}

import scala.math.{Pi, pow}

private object Universe {
  private val UniverseShader: Shader = Shader.load("universe")
}

/**
 * A game universe.
 *
 * Universes contain objects in a particular (definite) state that can interact with each other, but not with objects
 * from other universes. It corresponds to a basis vector with a particular amplitude (coefficient) in a quantum state.
 *
 * @param size the number of bits in this universe
 */
private class Universe(size: Int) {
  import Universe._

  /**
   * The probability amplitude of this universe.
   */
  var amplitude: Complex = Complex(1)

  /**
   * The bits in this universe.
   */
  var bits: Array[Bit] = Array.tabulate(size)(i => Bit(new Vec2d(1 + i, 1)))

  /**
   * The state of this universe, given by Σ,,i,, b,,i,, · 2^i^, where b,,i,, is the state of the ith bit.
   */
  def state: Int =
    bits.zipWithIndex.map { case (p, i) => if (p.on) pow(2, i).toInt else 0 }.sum

  /**
   * Creates a deep copy of this universe.
   *
   * @return a deep copy of this universe
   */
  def copy(): Universe = {
    val u = new Universe(size)
    u.amplitude = amplitude
    u.bits = bits.map(_.copy())
    u
  }

  /**
   * Steps physics forward for this universe.
   */
  def step(): Unit = bits.foreach(_.step())

  /**
   * Draws this universe.
   *
   * @param time the current game time
   * @param minValue the minimum value of the noise range for this universe
   * @param maxValue the maximum value of the noise range for this universe
   */
  def draw(time: Double, minValue: Double, maxValue: Double): Unit = {
    bits.foreach(_.draw())

    val camera = new Camera2d()
    camera.lowerLeft = new Vec2d(-1, -1)
    Camera.current = camera
    UniverseShader.setMVP(Transformation.IDENTITY)
    UniverseShader.setUniform("time", time.asInstanceOf[Float])
    UniverseShader.setUniform("minVal", minValue.asInstanceOf[Float])
    UniverseShader.setUniform("maxVal", maxValue.asInstanceOf[Float])
    UniverseShader.setUniform("hue", (amplitude.phase / (2 * Pi)).asInstanceOf[Float])
    Camera.current = Camera.camera2d
  }
}
