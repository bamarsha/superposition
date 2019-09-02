package superposition

import engine.core.Behavior.Entity
import engine.core.Game
import extras.physics.PhysicsComponent

import scala.collection.immutable.HashMap
import scala.math.pow

/**
 * A game universe.
 *
 * Universes contain objects in a particular (definite) state that can interact with each other, but not with objects
 * from other universes. It corresponds to a basis vector with a particular amplitude (coefficient) in a quantum state.
 *
 * @param multiverse the multiverse this universe belongs to
 */
private final class Universe(multiverse: Multiverse) extends Entity {
  /**
   * The probability amplitude of this universe.
   */
  var amplitude: Complex = Complex(1)

  private var _objects: Map[UniversalId, UniverseObject] = new HashMap[UniversalId, UniverseObject]()

  private var _physicsObjects: Map[UniversalId, PhysicsComponent] = new HashMap[UniversalId, PhysicsComponent]()

  private var _qubits: Map[UniversalId, Qubit] = new HashMap[UniversalId, Qubit]()

  override protected def onCreate(): Unit = objects.values.foreach(o => Game.create(o.entity))

  override protected def onDestroy(): Unit = objects.values.foreach(o => Game.destroy(o.entity))

  /**
   * The objects in this universe.
   */
  def objects: Map[UniversalId, UniverseObject] = _objects

  /**
   * The objects with physics in this universe.
   */
  def physicsObjects: Map[UniversalId, PhysicsComponent] = _physicsObjects

  /**
   * The qubits in this universe.
   */
  def qubits: Map[UniversalId, Qubit] = _qubits

  /**
   * The state of this universe, given by Σ,,i,, q,,i,, · 2^i^, where q,,i,, is the state of the ith qubit.
   */
  def state: Int =
    qubits.values.map(q => if (q.on) pow(2, q.universeObject.id.value).toInt else 0).sum

  /**
   * The list of walls in this universe.
   */
  def walls: List[Wall] = multiverse.walls

  /**
   * Returns a copy of this universe and all of its objects.
   *
   * @return a copy of this universe
   */
  def copy(): Universe = {
    val universe = new Universe(multiverse)
    universe.amplitude = amplitude
    for (e <- objects.values.map(_.entity)) {
      val copy = e.copy()
      copy.get(classOf[UniverseObject]).universe = universe
      universe.add(copy)
    }
    universe
  }

  /**
   * Adds the entity to this universe.
   *
   * @param entity the entity to add
   * @throws IllegalArgumentException if the entity does not have a [[superposition.UniverseObject]] component
   * @throws IllegalArgumentException if the entity's universe is not the same as this universe
   * @throws IllegalArgumentException if the entity's universal ID has already been used in this universe
   */
  def add(entity: Entity): Unit = {
    require(entity.has(classOf[UniverseObject]), "Entity is not a universe object")
    val universeObject = entity.get(classOf[UniverseObject])
    require(universeObject.universe == this, "Entity is not a part of this universe")
    require(!objects.contains(universeObject.id), "ID has already been used")

    _objects += (universeObject.id -> universeObject)
    if (entity.has(classOf[PhysicsComponent])) {
      _physicsObjects += (universeObject.id -> entity.get(classOf[PhysicsComponent]))
    }
    if (entity.has(classOf[Qubit])) {
      _qubits += (universeObject.id -> entity.get(classOf[Qubit]))
    }
  }
}
