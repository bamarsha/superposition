package superposition.quantum

import superposition.game.{Player, Quball, UniverseComponent}
import superposition.math.{Cell, Complex}

/**
 * A game universe.
 *
 * Universes contain objects in a particular (definite) state that can interact with each other, but not with objects
 * from other universes. It corresponds to a basis vector with a particular amplitude (coefficient) in a quantum state.
 */
final case class Universe(amplitude: Complex = Complex(1), state: IdMap = IdMap.empty,
                          metadata: IdMap = IdMap.empty, walls: Set[Cell] = Set.empty) {
  def +(c: Complex): Universe = copy(amplitude = amplitude + c)
  def -(c: Complex): Universe = copy(amplitude = amplitude - c)
  def *(c: Complex): Universe = copy(amplitude = amplitude * c)
  def /(c: Complex): Universe = copy(amplitude = amplitude / c)

  def get(i: Id[_]): i.T = state.get(i)
  def set(i: Id[_])(t: i.T): Universe = copy(state = state.set(i)(t))
  def getMeta(i: Id[_]): i.T = metadata.get(i)
  def setMeta(i: Id[_])(t: i.T): Universe = copy(metadata = metadata.set(i)(t))

  def allInCell(c: Cell): Iterable[UniverseComponent] =
    UniverseComponent.All.filter(_.position.map(this.get).contains(c))
  def getPrimaryBits(c: Cell): Iterable[Id[Boolean]] = allInCell(c).flatMap(_.primaryBit.toList)
  def isBlocked(c: Cell): Boolean =
    walls.contains(c) || UniverseComponent.All.exists(_.blockingCells(this).contains(c))
  def allOn(controls: List[Cell]): Boolean =
    controls.forall(c => Quball.All.exists(q => get(q.cell) == c && get(q.onOff)))

  def isValid: Boolean = Player.All.forall(p => !isBlocked(this.get(p.cell)))
}
