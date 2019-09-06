package superposition

import engine.core.Behavior.{Component, Entity}
import engine.core.Game
import engine.util.math.Vec2d
import extras.physics.{PhysicsComponent, PositionComponent, Rectangle}

import scala.collection.immutable.HashMap
import scala.math.pow

/**
 * Represents the ID of a universe object.
 *
 * A universal ID is unique within a universe but not within the multiverse; copies of an entity in other universes will
 * have the same ID.
 *
 * @param value the ID
 */
private final case class UniversalId(value: Int) extends AnyVal

/**
 * A game universe.
 *
 * Universes contain objects in a particular (definite) state that can interact with each other, but not with objects
 * from other universes. It corresponds to a basis vector with a particular amplitude (coefficient) in a quantum state.
 *
 * @param multiverse the multiverse this universe belongs to
 */
private final class Universe(multiverse: Multiverse) extends Entity with Copyable[Universe] {
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
  def walls: List[Rectangle] = multiverse.walls

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

  /**
   * Applies the quantum logic gate to the target qubit.
   *
   * @param gate   the gate to apply
   * @param target the target qubit
   */
  def applyGate(gate: Gate.Value, target: UniversalId): Unit =
  // TODO: Batch gates applied to the same qubit from different universes in the same frame.
    multiverse.applyGate(gate, target)

  /**
   * Returns a copy of this universe and all of its objects.
   *
   * @return a copy of this universe
   */
  override def copy(): Universe = {
    val universe = new Universe(multiverse)
    universe.amplitude = amplitude
    for (e <- objects.values.map(_.entity)) {
      val copy = e.copy()
      copy.get(classOf[UniverseObject]).universe = universe
      universe.add(copy)
    }
    universe
  }
}

/**
 * A universe object is any object that exists within a particular universe.
 * <p>
 * Universe objects <em>must</em> perform all drawing in their [[superposition.Drawable#draw]] method. This makes it
 * possible for the multiverse to draw copies of an object from different universes in superposition.
 *
 * @param entity              the entity for this component
 * @param universe            the universe this object belongs to
 * @param id                  the ID of this object
 * @param hitboxSize          the size of this object's hitbox
 * @param collidesWithObjects whether this object collides with other objects in the universe (excluding walls)
 */
private final class UniverseObject(entity: Entity with Copyable[_ <: Entity] with Drawable,
                                   var universe: Universe,
                                   val id: UniversalId,
                                   val hitboxSize: Vec2d = new Vec2d(0, 0),
                                   var collidesWithObjects: Boolean = false) extends Component(entity) {
  /**
   * The position component of this object.
   */
  lazy val position: PositionComponent = get(classOf[PositionComponent])

  /**
   * The hitbox for this object.
   */
  def hitbox: Rectangle =
    Rectangle.fromCenterSize(position.value, hitboxSize)

  /**
   * Returns true if this object would collide with any other object or wall at the position.
   *
   * @param position the position to test for collision
   * @return true if this object would collide with any other object or wall at the position
   */
  def collides(position: Vec2d): Boolean = {
    val hitbox = Rectangle.fromCenterSize(position, hitboxSize)
    val otherObjects = universe.objects.values
      .filter(o => o.entity != (this: Component[_]).entity && o.collidesWithObjects)
      .map(o => Rectangle.fromCenterSize(o.position.value, o.hitboxSize))
    universe.walls.appendedAll(otherObjects).exists(hitbox.intersects)
  }
}
