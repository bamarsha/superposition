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
 * The index of each quball in the vector must correspond to its qubit number.
 *
 * @param quballs the quballs in this universe
 * @throws IllegalArgumentException if quball indices do not match qubit numbers
 */
private class Universe(val quballs: Vector[Quball]) extends Entity {
  scala.Predef.require(
    quballs.zipWithIndex.forall { case (q, i) => q.qubit == i },
    "quball indices do not match qubit numbers"
  )

  /**
   * The probability amplitude of this universe.
   */
  var amplitude: Complex = Complex(1)

  /**
   * Creates a universe with quballs in default positions and states.
   *
   * @param size the number of quballs in this universe
   */
  def this(size: Int) =
    this(Vector.tabulate(size)(i => new Quball(i, new Vec2d(1 + i, 1))))

  override protected def onCreate(): Unit = quballs.foreach(_.create())

  override protected def onDestroy(): Unit = quballs.foreach(_.destroy())
  
  /**
   * The state of this universe, given by Σ,,i,, b,,i,, · 2^i^, where b,,i,, is the state of the ith qubit.
   */
  def state: Int = quballs.map(q => if (q.on) pow(2, q.qubit).toInt else 0).sum

  /**
   * Creates a deep copy of this universe.
   *
   * @return a deep copy of this universe
   */
  def copy(): Universe = {
    val u = new Universe(quballs.map(_.copy()))
    u.amplitude = amplitude
    u
  }

  /**
   * Draws this universe.
   */
  def draw(): Unit = quballs.foreach(_.draw())
}
