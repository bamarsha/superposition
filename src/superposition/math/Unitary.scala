package superposition.math

import cats.data.NonEmptyList

/** A quantum unitary transformation. */
trait Unitary {

  /** Applies the gate within a universe.
    *
    * @param universe the universe in which to apply the unitary
    * @return the universes produced by the unitary
    */
  def apply(universe: Universe): NonEmptyList[Universe]

  /** The reverse of the unitary. */
  def adjoint: Unitary
}

object Unitary {

  /** Operations on unitaries.
    *
    * @param unitary the unitary to apply the operations to
    */
  implicit final class Ops(val unitary: Unitary) extends AnyVal {

    /** The product of this unitary with another. */
    def *(unitary2: Unitary): Unitary = new Unitary {
      override def apply(universe: Universe): NonEmptyList[Universe] = unitary2.apply(universe) flatMap unitary.apply
      override def adjoint: Unitary = unitary2.adjoint * unitary.adjoint
    }
  }

  val identity: Unitary = new Unitary {
    override def apply(universe: Universe): NonEmptyList[Universe] = NonEmptyList.of(universe)
    override def adjoint: Unitary = identity
  }
}
