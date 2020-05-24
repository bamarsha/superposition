package superposition.component

import cats.implicits.catsStdInstancesForList
import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps
import cats.syntax.traverse.toTraverseOps
import com.badlogic.ashley.core._
import superposition.component.Multiverse.combine
import superposition.math.QExpr.QExpr
import superposition.math._

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
  private var _stateIds: List[StateId[_]] = List()

  /** The universes in the multiverse. */
  def universes: Seq[Universe] = _universes

  /** The entities in the multiverse. */
  def entities: Iterable[Entity] = _entities

  /** The list of qudit state IDs in the multiverse. */
  def stateIds: List[StateId[_]] = _stateIds

  /** Adds an entity to the multiverse.
    *
    * @param entity the entity to add
    */
  def addEntity(entity: Entity): Unit = _entities ::= entity

  /** Allocates a qudit.
    *
    * @param name the name of the qudit
    * @param initialValue the initial value of the qudit
    * @param showValue a function that maps values of the qudit to strings
    * @tparam A the qudit's type
    * @return the qudit's ID
    */
  def allocate[A](name: String, initialValue: A, showValue: A => String = (_: A).toString): StateId[A] = {
    val id = new StateId[A](name, showValue)
    _universes = _universes map (_.updatedState(id)(initialValue))
    _stateIds = _stateIds.appended(id)
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
    * @param updater a function that receives the metadata's value returns the new value
    */
  def updateMetaWith(id: MetaId[_])(updater: id.Value => QExpr[id.Value]): Unit =
    _universes = _universes map (universe => universe.updatedMetaWith(id)(updater(_)(universe)))

  /** Applies a gate. If the gate produces any universe that is in an invalid state, no changes are made.
    *
    * @param gate the gate to apply
    * @param value the gate's argument
    * @return true if the gate was successfully applied
    */
  def applyGate[A](gate: Gate[A], value: A): Boolean = {
    val newUniverses = gate.applyToAll(value)(universes)
    if (newUniverses forall (isValid(_))) {
      _universes = combine(newUniverses).toSeq sortBy (showUniverse andThen (_.toSeq))
      true
    } else false
  }

  /** Returns the entities occupying the cell.
    *
    * @param cell the cell to look at
    * @return the entities occupying the cell
    */
  def allInCell(cell: Vector2[Int]): QExpr[Iterable[Entity]] =
    for (currentCell <- QExpr.prepare(QuantumPosition.mapper.get(_: Entity).cell.value)) yield
      entities filter (entity => QuantumPosition.mapper.has(entity) && currentCell(entity) == cell)

  /** Returns all primary qubits in the cell.
    *
    * @param cell the cell to look at
    * @return the primary qubits in the cell
    */
  def primaryBits(cell: Vector2[Int]): QExpr[Iterable[Seq[StateId[Boolean]]]] =
    allInCell(cell) map (_ filter PrimaryBit.mapper.has map (PrimaryBit.mapper.get(_).bits))

  /** Returns true if the cell is blocked by an entity with collision.
    *
    * @param cell the cell to look at
    * @return true if the cell is blocked by an entity with collision
    */
  def isBlocked(cell: Vector2[Int]): QExpr[Boolean] =
    for (cells <- QExpr.prepare(Collider.mapper.get(_: Entity).cells)) yield
      walls.contains(cell) || (entities filter Collider.mapper.has exists (cells(_).contains(cell)))

  /** Returns true in index idx if this cell has at least one |1⟩ activator in index idx
    *
    * @param cell the cell to look at
    * @return true if all cells have at least one activator qubit in the |1⟩ state
    */
  def activation(cell: Vector2[Int]): QExpr[BitSeq] =
    for {
      entities <- allInCell(cell)
      bit <- QExpr.prepare((_: StateId[Boolean]).value)
    } yield
      entities
        .filter(Activator.mapper.has)
        .map(Activator.mapper.get(_).bits map bit)
        .map(BitSeq(_: _*))
        .fold(BitSeq())(_ or _)

  /** Returns true in index idx if all cells have at least one |1⟩ activator in index idx
    *
    * @param cells the cells to look at
    * @return true if all cells have at least one activator qubit in the |1⟩ state
    */
  def allActivated(cells: Iterable[Vector2[Int]]): QExpr[BitSeq] =
    if (cells.isEmpty) BitSeq().pure[QExpr]
    else (cells map activation).toList.sequence map (_ reduce (_ and _))

  /** Returns true if every entity has a valid position in the universe.
    *
    * @return true if every entity has a valid position in the universe
    */
  private val isValid: QExpr[Boolean] =
    for {
      cell <- QExpr.prepare(QuantumPosition.mapper.get(_: Entity).cell.value)
      blocked <- QExpr.prepare(isBlocked)
    } yield entities filter QuantumPosition.mapper.has forall (cell andThen blocked andThen (!_))

  /** Shows all states in the universe.
    *
    * @param universe the universe
    * @return the states in the universe converted to strings
    */
  def showUniverse(universe: Universe): Iterable[String] =
    stateIds.view map (id => /*_*/ id.show(universe.state(id)) /*_*/)

  /** Returns the entity with the ID.
    *
    * @param id the ID
    * @return the entity with the ID
    */
  def entityById(id: Int): Option[Entity] =
    entities filter EntityId.mapper.has find (EntityId.mapper.get(_).id == id)
}

/** Contains the component mapper for the multiverse component. */
object Multiverse {
  /** The component mapper for the multiverse component. */
  val mapper: ComponentMapper[Multiverse] = ComponentMapper.getFor(classOf[Multiverse])

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
    normalize(universes
                .groupMapReduce(_.state)(identity)(_ + _.amplitude)
                .values
                .filter(_.amplitude.squaredMagnitude > 1e-6))
}
