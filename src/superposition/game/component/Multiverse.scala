package superposition.game.component

import com.badlogic.ashley.core._
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import scalaz.Scalaz._
import superposition.game.component.Multiverse.combine
import superposition.game.entity.Quball
import superposition.math.{Complex, Vector2i}
import superposition.quantum.{Gate, MetaId, StateId, Universe}

import scala.Ordering.Implicits._
import scala.math.sqrt

final class Multiverse(val walls: Set[Vector2i], val camera: OrthographicCamera) extends Component {
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
      _universes =
        (newUniverses
          |> combine
          |> (_.toSeq)
          |> (_ sortBy (universe => stateIds.reverse map (universe.state(_).toString))))
      true
    } else false
  }

  def allInCell(universe: Universe, cell: Vector2i): Iterable[Entity] =
    entities filter { entity =>
      Position.Mapper.has(entity) && universe.state(Position.Mapper.get(entity).cell) == cell
    }

  def toggles(universe: Universe, cell: Vector2i): Iterable[StateId[Boolean]] =
    allInCell(universe, cell) flatMap { entity =>
      if (Toggle.Mapper.has(entity)) Some(Toggle.Mapper.get(entity).toggle)
      else None
    }

  def isBlocked(universe: Universe, cell: Vector2i): Boolean =
    walls.contains(cell) ||
      (entities
        filter Collider.Mapper.has
        exists (Collider.Mapper.get(_).cells(universe).contains(cell)))

  def allOn(universe: Universe, controls: Iterable[Vector2i]): Boolean =
    controls forall { control =>
      entities filter (_.isInstanceOf[Quball]) exists { quball =>
        universe.state(quball.asInstanceOf[Quball].cell) == control && universe.state(quball.asInstanceOf[Quball].onOff)
      }
    }

  def isValid(universe: Universe): Boolean =
    entities filter Position.Mapper.has forall { entity =>
      !isBlocked(universe, universe.state(Position.Mapper.get(entity).cell))
    }

  def isSelected(cell: Vector2i): Boolean = {
    val mouse = camera.unproject(new Vector3(input.getX, input.getY, 0))
    cell == Vector2i(mouse.x.floor.toInt, mouse.y.floor.toInt)
  }
}

object Multiverse {
  val Mapper: ComponentMapper[Multiverse] = ComponentMapper.getFor(classOf[Multiverse])

  private def normalize(universes: Iterable[Universe]): Iterable[Universe] = {
    val sum = (universes map (_.amplitude.squaredMagnitude)).sum
    universes map (_ / Complex(sqrt(sum)))
  }

  private def combine(universes: Iterable[Universe]): Iterable[Universe] =
    (universes
      .groupMapReduce(_.state)(identity)(_ + _.amplitude)
      .values
      .filter(_.amplitude.squaredMagnitude > 1e-6)
      |> normalize)
}
