package superposition

import engine.core.Behavior
import engine.core.Behavior.Entity
import engine.util.math.Vec2d

import scala.math.pow

import scala.jdk.CollectionConverters._

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
private class Universe extends Entity {
  /**
   * The probability amplitude of this universe.
   */
  var amplitude: Complex = Complex(1)

  override def onDestroy(): Unit = objects.foreach(_.destroy())

  private def objects: Iterable[Physics] =
    Behavior.track(classOf[Physics]).asScala.filter(_.universe eq this)

  def qubits: Iterable[Qubit] =
    Behavior.track(classOf[Qubit]).asScala.filter(_.physics.universe eq this)

  def qubit(id: Int): Qubit = qubits.find(_.id == id).get

  /**
   * The state of this universe, given by Σ,,i,, q,,i,, · 2^i^, where q,,i,, is the state of the ith qubit.
   */
  def state: Int =
    qubits.map(q => if (q.on) pow(2, q.id).toInt else 0).sum

  def create(size: Int): Unit = {
    for (i <- 0 until size) {
      new Quball(i, false, new Vec2d(1 + i, 1), this).create()
    }
  }

  /**
   * Creates a copy of this universe and all of its objects.
   *
   * @return a copy of this universe
   */
  def copy(): Universe = {
    val universe = new Universe()
    objects.foreach(_.copy(universe))
    universe.amplitude = amplitude
    universe
  }

  /**
   * Draws this universe.
   */
  def draw(): Unit = objects.foreach(_.draw())
}
