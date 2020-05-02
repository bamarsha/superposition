package superposition.game

import com.badlogic.ashley.core._
import com.badlogic.gdx.graphics.OrthographicCamera
import superposition.game.Multiverse.BasicStateMapper
import superposition.math.Vector2i
import superposition.quantum.{MetaId, StateId, Universe}

private final class Multiverse(val camera: OrthographicCamera) extends Component {
  private var _universes: Seq[Universe] = Seq(Universe())

  private var _entities: List[Entity] = List()

  private var stateIds: List[StateId[_]] = List()

  def universes: Seq[Universe] = _universes

  def entities: Iterable[Entity] = _entities

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

  def addEntity(entity: Entity): Unit = _entities ::= entity

  def allInCell(universe: Universe, cell: Vector2i): Iterable[BasicState] =
    _entities map BasicStateMapper.get filter (_.position map (universe.state(_)) contains cell)

  def primaryBits(universe: Universe, cell: Vector2i): Iterable[StateId[Boolean]] =
    allInCell(universe, cell) flatMap (_.primaryBit.toList)

  def isBlocked(universe: Universe, cell: Vector2i): Boolean =
    _entities map BasicStateMapper.get exists (_.blockingCells(universe) contains cell)

  def allOn(universe: Universe, controls: Iterable[Vector2i]): Boolean =
    controls forall { control =>
      _entities filter (_.isInstanceOf[Quball]) exists { quball =>
        universe.state(quball.asInstanceOf[Quball].cell) == control && universe.state(quball.asInstanceOf[Quball].onOff)
      }
    }

  def isValid(universe: Universe): Boolean =
    _entities map BasicStateMapper.get forall { basicState =>
      basicState.position.isEmpty || !isBlocked(universe, universe.state(basicState.position.get))
    }
}

private object Multiverse {
  private val BasicStateMapper: ComponentMapper[BasicState] = ComponentMapper.getFor(classOf[BasicState])
}
