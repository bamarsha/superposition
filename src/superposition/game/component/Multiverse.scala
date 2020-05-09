package superposition.game.component

import com.badlogic.ashley.core._
import scalaz.Scalaz._
import superposition.game.component.Multiverse.combine
import superposition.math.{Complex, Vector2}
import superposition.quantum.{Gate, MetaId, StateId, Universe}

import scala.Ordering.Implicits._
import scala.math.sqrt

/** The multiverse component is the quantum system for a level.
  *
  * @param walls the set of cells in the multiverse that always have collision
  */
final class Multiverse(val walls: Set[Vector2[Int]]) extends Component {
  /** The universes in the multiverse. */
  private var _universes: Seq[Universe] = Seq(Universe())

  /** The entities in the multiverse. */
  private var _entities: List[Entity] = List()

  /** The list of qudit state IDs in the multiverse. */
  private var stateIds: List[StateId[_]] = List()

  /** The universes in the multiverse. */
  def universes: Seq[Universe] = _universes

  /** The entities in the multiverse. */
  def entities: Iterable[Entity] = _entities

  /** Adds an entity to the multiverse.
    *
    * @param entity the entity to add
    */
  def addEntity(entity: Entity): Unit = _entities ::= entity

  /** Allocates a qudit.
    *
    * @param initialValue the qudit's initial value
    * @tparam A the qudit's type
    * @return the qudit's ID
    */
  def allocate[A](initialValue: A): StateId[A] = {
    val id = new StateId[A]
    _universes = _universes map (_.updatedState(id)(initialValue))
    stateIds ::= id
    id
  }

  /** Allocates a piece of metadata.
    *
    * @param initialValue the metadata's initial value
    * @tparam A the metadata's type
    * @return the metadata's ID
    */
  def allocateMeta[A](initialValue: A): MetaId[A] = {
    val id = new MetaId[A]
    _universes = _universes map (_.updatedMeta(id)(initialValue))
    id
  }

  /** Updates a piece of metadata in every universe.
    *
    * @param id the metadata's ID
    * @param updater a function that receives the metadata's value and the universe and returns the new value
    */
  def updateMetaWith(id: MetaId[_])(updater: id.Value => Universe => id.Value): Unit =
    _universes = _universes map (universe => universe.updatedMetaWith(id)(updater(_)(universe)))

  /** Applies a gate. If the gate produces any universe that is in an invalid state, no changes are made.
    *
    * @param gate the gate to apply
    * @param value the gate's argument
    * @return true if the gate was successfully applied
    */
  def applyGate[A](gate: Gate[A], value: A): Boolean = {
    val newUniverses = gate.applyToAll(value)(universes)
    if (newUniverses forall isValid) {
      _universes =
        (newUniverses
          |> combine
          |> (_.toSeq)
          |> (_ sortBy (universe => stateIds.reverse map (universe.state(_).toString))))
      true
    } else false
  }

  /** Returns the entities occupying the cell.
    *
    * @param universe the universe to look in
    * @param cell the cell to look at
    * @return the entities occupying the cell
    */
  def allInCell(universe: Universe, cell: Vector2[Int]): Iterable[Entity] =
    entities filter { entity =>
      QuantumPosition.Mapper.has(entity) && universe.state(QuantumPosition.Mapper.get(entity).cell) == cell
    }

  /** Returns the toggleable qubits occupying the cell.
    *
    * @param universe the universe to look in
    * @param cell the cell to look at
    * @return the toggleable qubits occupying the cell
    */
  def toggles(universe: Universe, cell: Vector2[Int]): Iterable[StateId[Boolean]] =
    allInCell(universe, cell) flatMap { entity =>
      if (Toggle.Mapper.has(entity)) Some(Toggle.Mapper.get(entity).toggle)
      else None
    }

  /** Returns true if the cell is blocked by an entity with collision.
    *
    * @param universe the universe to look in
    * @param cell the cell to look at
    * @return true if the cell is blocked by an entity with collision
    */
  def isBlocked(universe: Universe, cell: Vector2[Int]): Boolean =
    walls.contains(cell) ||
      (entities
        filter Collider.Mapper.has
        exists (Collider.Mapper.get(_).cells(universe).contains(cell)))

  /** Returns true if all control cells have at least one activator qubit in the |1⟩ state.
    *
    * @param universe the universe to look in
    * @param controls the control cells to look at
    * @return true if all control cells have at least one activator qubit in the |1⟩ state
    */
  def allOn(universe: Universe, controls: Iterable[Vector2[Int]]): Boolean =
    controls forall { control =>
      entities exists { entity =>
        Activator.Mapper.has(entity) && QuantumPosition.Mapper.has(entity) &&
          universe.state(Activator.Mapper.get(entity).activator) &&
          universe.state(QuantumPosition.Mapper.get(entity).cell) == control
      }
    }

  /** Returns true if every entity has a valid position in the universe.
    *
    * @param universe the universe to check for validity
    * @return true if every entity has a valid position in the universe
    */
  private def isValid(universe: Universe): Boolean =
    entities filter QuantumPosition.Mapper.has forall { entity =>
      !isBlocked(universe, universe.state(QuantumPosition.Mapper.get(entity).cell))
    }
}

/** Contains the component mapper for the multiverse component. */
object Multiverse {
  /** The component mapper for the multiverse component. */
  val Mapper: ComponentMapper[Multiverse] = ComponentMapper.getFor(classOf[Multiverse])

  /** Normalizes the total probability amplitude of the universes to 1.
    *
    * @param universes the universes to normalize
    * @return the normalized universes
    */
  private def normalize(universes: Iterable[Universe]): Iterable[Universe] = {
    val sum = (universes map (_.amplitude.squaredMagnitude)).sum
    universes map (_ / Complex(sqrt(sum)))
  }

  /** Combines universes with the same state and discards any resulting universes with very low probability amplitude.
    *
    * @param universes the universes to combine
    * @return the combined universes
    */
  private def combine(universes: Iterable[Universe]): Iterable[Universe] =
    (universes
      .groupMapReduce(_.state)(identity)(_ + _.amplitude)
      .values
      .filter(_.amplitude.squaredMagnitude > 1e-6)
      |> normalize)
}
