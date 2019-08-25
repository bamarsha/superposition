package superposition

import engine.core.Behavior.Entity
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
private class Universe(size: Int) extends Entity {

  /**
   * The probability amplitude of this universe.
   */
  var amplitude: Complex = Complex(1)

  /**
   * The quballs in this universe.
   */
  var quballs: Array[Quball] = Array.tabulate(size)(i => new Quball(new Vec2d(1 + i, 1)))

  override protected def onCreate(): Unit = quballs.foreach(_.create())

  override protected def onDestroy(): Unit = quballs.foreach(_.destroy())
  
  /**
   * The state of this universe, given by Σ,,i,, b,,i,, · 2^i^, where b,,i,, is the state of the ith bit.
   */
  def state: Int =
    quballs.zipWithIndex.map { case (p, i) => if (p.on) pow(2, i).toInt else 0 }.sum

  /**
   * Creates a deep copy of this universe.
   *
   * @return a deep copy of this universe
   */
  def copy(): Universe = {
    val u = new Universe(size)
    u.amplitude = amplitude
    u.quballs = quballs.map(_.copy())
    u
  }

  /**
   * Draws this universe.
   */
  def draw(): Unit = quballs.foreach(_.draw())
}
