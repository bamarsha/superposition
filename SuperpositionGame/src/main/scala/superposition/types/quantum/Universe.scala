package superposition.types.quantum

import superposition.game.{Player, UniverseComponent}
import superposition.types.math.{Cell, Complex}

/**
 * A game universe.
 *
 * Universes contain objects in a particular (definite) state that can interact with each other, but not with objects
 * from other universes. It corresponds to a basis vector with a particular amplitude (coefficient) in a quantum state.
 */
case class Universe(amplitude: Complex = Complex(1), state: IdMap = IdMap.empty) {
  def +(c: Complex): Universe = Universe(amplitude + c, state)
  def -(c: Complex): Universe = Universe(amplitude - c, state)
  def *(c: Complex): Universe = Universe(amplitude * c, state)
  def /(c: Complex): Universe = Universe(amplitude / c, state)
  def get(i: Id[_]): i.T = state.get(i)
  def set(i: Id[_])(t: i.T): Universe = Universe(amplitude, state.set(i)(t))

  def allInCell(c: Cell): Iterable[UniverseComponent] = UniverseComponent.All
    .filter(_.position.map(this.get).contains(c))
  def getPrimaryBits(c: Cell): Iterable[Id[Boolean]] = allInCell(c).flatMap(_.primaryBit.toList)
  def isBlocked(c: Cell): Boolean = UniverseComponent.All.exists(_.blockingCells(this).contains(c))

  def isValid: Boolean = Player.All.forall(p => !isBlocked(this.get(p.qPosition)))
}
