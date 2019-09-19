package superposition

import engine.core.Behavior.Entity
import engine.core.Game

import scala.collection.immutable.HashMap

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

  private var _objects: Map[ObjectId, UniverseObject] = new HashMap

  private var _bitMaps: Map[ObjectId, BitMap] = new HashMap

  override protected def onCreate(): Unit = objects.values.foreach(o => Game.create(o.entity))

  override protected def onDestroy(): Unit = objects.values.foreach(o => Game.destroy(o.entity))

  /**
   * The objects in this universe.
   */
  def objects: Map[ObjectId, UniverseObject] = _objects

  /**
   * The bit maps in this universe.
   */
  def bitMaps: Map[ObjectId, BitMap] = _bitMaps

  /**
   * An opaque representation of this universe's state that can be compared for equality with the state of other
   * universes.
   */
  def state: Equals =
    bitMaps.view.mapValues(q => (q.state, q.obj.cell)).toMap

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
    val obj = entity.get(classOf[UniverseObject])
    require(obj.universe == this, "Entity is not a part of this universe")
    require(!objects.contains(obj.id), "ID has already been used")

    _objects += obj.id -> obj
    if (entity.has(classOf[BitMap])) {
      _bitMaps += obj.id -> entity.get(classOf[BitMap])
    }
  }

  /**
   * Returns the IDs of objects with bit maps that are in the cell in this universe.
   *
   * @param cell the cell in which to find objects with bit maps
   * @return the IDs of objects with bit maps that are in the cell in this universe
   */
  def bitsInCell(cell: Cell): Set[ObjectId] =
    bitMaps.values
      .filter(_.obj.cell == cell)
      .map(_.obj.id)
      .toSet

  /**
   * Returns true if the cell is open for movement (i.e., does not contain any objects with collision).
   *
   * @param cell the cell to test for movement
   * @return true if the cell is open for movement
   */
  def cellOpen(cell: Cell): Boolean =
    !multiverse.walls.contains(cell) && objects.values.filter(_.collision).forall(_.cell != cell)

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
