package superposition

import engine.core.Behavior.{Component, Entity}
import engine.core.Game
import engine.util.math.Vec2d
import extras.physics.PositionComponent

import scala.collection.immutable.HashMap

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
private final class Universe(val multiverse: Multiverse) extends Entity with Copyable[Universe] {
  /**
   * The probability amplitude of this universe.
   */
  var amplitude: Complex = Complex(1)

  private var _objects: Map[UniversalId, UniverseObject] = new HashMap()

  private var _bits: Map[UniversalId, Bit] = new HashMap()

  override protected def onCreate(): Unit = objects.values.foreach(o => Game.create(o.entity))

  override protected def onDestroy(): Unit = objects.values.foreach(o => Game.destroy(o.entity))

  /**
   * The objects in this universe.
   */
  def objects: Map[UniversalId, UniverseObject] = _objects

  /**
   * The bits in this universe.
   */
  def bits: Map[UniversalId, Bit] = _bits

  /**
   * An opaque representation of this universe's state that can be compared for equality with the state of other
   * universes.
   */
  def state: Equals =
    bits.view.mapValues(q => (q.on, q.universeObject.cell)).toMap

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
    if (entity.has(classOf[Bit])) {
      _bits += (universeObject.id -> entity.get(classOf[Bit]))
    }
  }

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
 * @param entity     the entity for this component
 * @param universe   the universe this object belongs to
 * @param id         the ID of this object
 * @param _cell      the initial grid cell of this object
 * @param collision  whether this object collides with other objects in the universe (excluding walls)
 */
private final class UniverseObject(entity: Entity with Copyable[_ <: Entity] with Drawable,
                                   var universe: Universe,
                                   val id: UniversalId,
                                   private var _cell: Cell,
                                   var collision: Boolean = false) extends Component(entity) {
  /**
   * The position component of this object.
   */
  lazy val position: PositionComponent = get(classOf[PositionComponent])

  /**
   * The multiverse this object belongs to.
   */
  val multiverse: Multiverse = universe.multiverse

  /**
   * The grid cell of this obejct.
   */
  def cell: Cell = _cell

  def cell_=(value: Cell): Unit = {
    _cell = value
    position.value = new Vec2d(value.column + 0.5, value.row + 0.5)
  }
}
