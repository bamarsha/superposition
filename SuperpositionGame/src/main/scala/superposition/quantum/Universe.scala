package superposition.quantum

import scalaz.Functor
import scalaz.std.option._
import superposition.game.{Player, Quball, UniverseComponent}
import superposition.math.{Cell, Complex, DependentHashMap, DependentMap}

/**
 * A game universe.
 *
 * Universes contain objects in a particular (definite) state that can interact with each other, but not with objects
 * from other universes. It corresponds to a basis vector with a particular amplitude (coefficient) in a quantum state.
 */
final case class Universe(amplitude: Complex = Complex(1),
                          state: DependentMap[Id[_]] = DependentHashMap.empty,
                          meta: DependentMap[Id[_]] = DependentHashMap.empty,
                          walls: Set[Cell] = Set.empty) {
  def +(c: Complex): Universe = copy(amplitude = amplitude + c)

  def -(c: Complex): Universe = copy(amplitude = amplitude - c)

  def *(c: Complex): Universe = copy(amplitude = amplitude * c)

  def /(c: Complex): Universe = copy(amplitude = amplitude / c)

  def updatedState(id: Id[_])(value: id.Value): Universe =
    copy(state = state.updated(id)(value))

  def updatedStateWith(id: Id[_])(updater: id.Value => id.Value): Universe =
    copy(state = state.updatedWith(id)(Functor[Option].lift(updater)))

  def updatedMeta(id: Id[_])(value: id.Value): Universe =
    copy(meta = meta.updated(id)(value))

  def updatedMetaWith(id: Id[_])(updater: id.Value => id.Value): Universe =
    copy(meta = meta.updatedWith(id)(Functor[Option].lift(updater)))

  def allInCell(cell: Cell): Iterable[UniverseComponent] =
    UniverseComponent.All filter (_.position map (state(_)) contains cell)

  def getPrimaryBits(cell: Cell): Iterable[Id[Boolean]] =
    allInCell(cell) flatMap (_.primaryBit.toList)

  def isBlocked(cell: Cell): Boolean =
    (walls contains cell) || (UniverseComponent.All exists (_.blockingCells(this) contains cell))

  def allOn(controls: List[Cell]): Boolean =
    controls forall {
      control => Quball.All exists (quball => state(quball.cell) == control && state(quball.onOff))
    }

  def isValid: Boolean = Player.All forall (player => !isBlocked(state(player.cell)))
}
