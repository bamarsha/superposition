package superposition.game

import com.badlogic.ashley.core._
import com.badlogic.gdx.graphics.OrthographicCamera
import scalaz.Scalaz._
import superposition.game.Multiverse.{BasicStateMapper, combine}
import superposition.math.{Complex, Vector2i}
import superposition.quantum.{Gate, MetaId, StateId, Universe}

import scala.Ordering.Implicits._
import scala.math.sqrt

private final class Multiverse(val camera: OrthographicCamera) extends Component {
  private var _universes: Seq[Universe] = Seq(Universe())

  private var _entities: List[Entity] = List()

  private var stateIds: List[StateId[_]] = List()

  def universes: Seq[Universe] = _universes

  def entities: Iterable[Entity] = _entities

  def addEntity(entity: Entity): Unit = _entities ::= entity

  def allocate[A](initialValue: A): StateId[A] = {
    val id = new StateId[A]
    _universes = _universes map (_.updatedState(id)(initialValue))
    stateIds ::= id
    id
  }

  def allocateMeta[A](initialValue: A): MetaId[A] = {
    val id = new MetaId[A]
    _universes = _universes map (_.updatedMeta(id)(initialValue))
    id
  }

  def updateMetaWith(id: MetaId[_])(updater: id.Value => Universe => id.Value): Unit =
    _universes = _universes map (universe => universe.updatedMetaWith(id)(updater(_)(universe)))

  /**
   * Applies a gate to the multiverse.
   * <p>
   * If the gate produces any universe that is in an invalid state, no changes are made.
   *
   * @param gate  the gate to apply
   * @param value the value to give the gate
   * @return true if the gate was successfully applied
   */
  def applyGate[A](gate: Gate[A], value: A): Boolean = {
    val newUniverses = gate.applyToAll(value)(universes)
    if (newUniverses forall isValid) {
      _universes = newUniverses |>
        combine |>
        (_.toSeq) |>
        (_ sortBy (universe => stateIds.reverse map (universe.state(_).toString)))
      true
    } else false
  }

  def allInCell(universe: Universe, cell: Vector2i): Iterable[BasicState] =
    entities
      .filter(BasicStateMapper.has)
      .map(BasicStateMapper.get)
      .filter(_.position map (universe.state(_)) contains cell)

  def primaryBits(universe: Universe, cell: Vector2i): Iterable[StateId[Boolean]] =
    allInCell(universe, cell) flatMap (_.primaryBit.toList)

  def isBlocked(universe: Universe, cell: Vector2i): Boolean =
    entities filter BasicStateMapper.has map BasicStateMapper.get exists (_.blockingCells(universe) contains cell)

  def allOn(universe: Universe, controls: Iterable[Vector2i]): Boolean =
    controls forall { control =>
      entities filter (_.isInstanceOf[Quball]) exists { quball =>
        universe.state(quball.asInstanceOf[Quball].cell) == control && universe.state(quball.asInstanceOf[Quball].onOff)
      }
    }

  def isValid(universe: Universe): Boolean =
    entities filter BasicStateMapper.has map BasicStateMapper.get forall { basicState =>
      basicState.position.isEmpty || !isBlocked(universe, universe.state(basicState.position.get))
    }
}

private object Multiverse {
  private val BasicStateMapper: ComponentMapper[BasicState] = ComponentMapper.getFor(classOf[BasicState])

  private def normalize(universes: Iterable[Universe]): Iterable[Universe] = {
    val sum = (universes map (_.amplitude.squaredMagnitude)).sum
    universes map (_ / Complex(sqrt(sum)))
  }

  private def combine(universes: Iterable[Universe]): Iterable[Universe] =
    universes
      .groupMapReduce(_.state)(identity)(_ + _.amplitude)
      .values
      .filter(_.amplitude.squaredMagnitude > 1e-6) |>
      normalize
}
