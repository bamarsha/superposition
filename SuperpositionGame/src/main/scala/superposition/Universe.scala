package superposition

import engine.core.Behavior.Entity
import engine.util.math.Vec2d

import scala.collection.immutable.{HashMap, HashSet}
import scala.math.pow

/**
 * A game universe.
 *
 * Universes contain game objects in a particular (definite) state that can interact with each other, but not with game
 * objects from other universes. It corresponds to a basis vector with a particular amplitude (coefficient) in a quantum
 * state.
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

  private var _gameObjects: Set[GameObject] = new HashSet[GameObject]()

  private var _qubits: Map[Int, Qubit] = new HashMap[Int, Qubit]()

  def gameObjects: Set[GameObject] = _gameObjects

  def qubits: Map[Int, Qubit] = _qubits

  override protected def onDestroy(): Unit = gameObjects.foreach(_.entity.destroy())

  /**
   * The state of this universe, given by Σ,,i,, q,,i,, · 2^i^, where q,,i,, is the state of the ith qubit.
   */
  def state: Int =
    qubits.values.map(q => if (q.on) pow(2, q.id).toInt else 0).sum

  def create(size: Int): Unit = {
    for (i <- 0 until size) {
      new Quball(this, i, false, new Vec2d(1 + i, 1)).create()
    }
    new Player(this, new Vec2d(0, 0)).create()
  }

  def add(gameObject: GameObject): Unit = _gameObjects += gameObject

  def add(qubit: Qubit): Unit = _qubits += (qubit.id -> qubit)

  /**
   * Creates a copy of this universe and all of its game objects.
   *
   * @return a copy of this universe
   */
  def copy(): Universe = {
    val universe = new Universe()
    gameObjects.foreach(_.copy(universe))
    universe.amplitude = amplitude
    universe
  }

  /**
   * Draws this universe.
   */
  def draw(): Unit = gameObjects.foreach(_.draw())
}
