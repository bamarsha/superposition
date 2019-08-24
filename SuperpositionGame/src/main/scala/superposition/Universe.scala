package superposition

import engine.util.math.Vec2d

import scala.math.pow

/**
 * A game universe.
 *
 * Universes contain objects in a particular (definite) state that can interact with each other, but not with objects
 * from other universes. It corresponds to a basis vector with a particular amplitude (coefficient) in a quantum state.
 *
 * @param size the number of bits in this universe
 */
private class Universe(size: Int) {
  /**
   * The probability amplitude of this universe.
   */
  var amplitude: Complex = Complex(1.0)

  /**
   * The bits in this universe.
   */
  var bits: Array[Bit] = Array.tabulate(size)(i => Bit(new Vec2d(i + 1.0, 1.0)))

  /**
   * The state of this universe, given by Σ,,i,, b,,i,, · 2^i^, where b,,i,, is the state of the ith bit.
   */
  def state: Int =
    bits.zipWithIndex.map { case (p, i) => if (p.on) pow(2, i).toInt else 0 }.sum

  /**
   * Steps physics forward for this universe.
   */
  def step(): Unit = bits.foreach(_.step())

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
}
