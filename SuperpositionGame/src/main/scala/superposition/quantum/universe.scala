package superposition.quantum

import scalaz.std.option._
import scalaz.syntax.functor._
import superposition.math._

/**
 * A game universe.
 *
 * Universes contain objects in a particular (definite) state that can interact with each other, but not with objects
 * from other universes. It corresponds to a basis vector with a particular amplitude (coefficient) in a quantum state.
 */
final case class Universe(amplitude: Complex = Complex(1),
                          state: DependentMap[StateId[_]] = DependentHashMap.empty,
                          meta: DependentMap[MetaId[_]] = DependentHashMap.empty) {
  def +(c: Complex): Universe = copy(amplitude = amplitude + c)

  def -(c: Complex): Universe = copy(amplitude = amplitude - c)

  def *(c: Complex): Universe = copy(amplitude = amplitude * c)

  def /(c: Complex): Universe = copy(amplitude = amplitude / c)

  def updatedState(id: StateId[_])(value: id.Value): Universe =
    copy(state = state.updated(id)(value))

  def updatedStateWith(id: StateId[_])(updater: id.Value => id.Value): Universe =
    copy(state = state.updatedWith(id)(updater.lift))

  def updatedMeta(id: MetaId[_])(value: id.Value): Universe =
    copy(meta = meta.updated(id)(value))

  def updatedMetaWith(id: MetaId[_])(updater: id.Value => id.Value): Universe =
    copy(meta = meta.updatedWith(id)(updater.lift))
}

final class StateId[A] extends DependentKey {
  type Value = A
}

final class MetaId[A] extends DependentKey {
  type Value = A
}
