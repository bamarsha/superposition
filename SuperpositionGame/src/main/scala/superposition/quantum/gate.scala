package superposition.quantum

import scalaz.{Divisible, NonEmptyList}
import superposition.math.{Complex, Vec2i}

import scala.math.sqrt

/**
 * A quantum gate.
 *
 * @tparam A The type of the gate's argument.
 */
sealed trait Gate[A] {
  /**
   * Applies a value to the gate within a universe.
   *
   * @param value    The value to apply.
   * @param universe The universe in which to apply the value.
   * @return The universes produced by the gate.
   */
  def apply(value: A)(universe: Universe): NonEmptyList[Universe]

  /**
   * The adjoint is the reverse of the gate.
   */
  def adjoint: Gate[A]
}

object Gate {

  implicit object GateDivisible extends Divisible[Gate] {
    override def conquer[A]: Gate[A] = Identity[A]()

    override def divide2[A, B, C](gate1: => Gate[A], gate2: => Gate[B])(f: C => (A, B)): Gate[C] = new Gate[C] {
      override def apply(value: C)(universe: Universe): NonEmptyList[Universe] = {
        val (a, b) = f(value)
        gate1(a)(universe) flatMap gate2(b)
      }

      override def adjoint: Gate[C] = divide2(gate2.adjoint, gate1.adjoint)(f(_).swap)
    }
  }

  final implicit class Ops[A](val gate: Gate[A]) extends AnyVal {

    import Gate.GateDivisible.divisibleSyntax._

    def applyToAll(value: A)(universes: Iterable[Universe]): Iterable[Universe] =
      universes flatMap (gate(value)(_).stream)

    def andThen(other: Gate[A]): Gate[A] =
      Divisible[Gate].divide(gate, other)(value => (value, value))

    def multi: Gate[Seq[A]] = new Gate[Seq[A]] {
      override def apply(values: Seq[A])(universe: Universe): NonEmptyList[Universe] = values match {
        case Nil => NonEmptyList(universe)
        case x :: xs => gate(x)(universe) flatMap gate.multi(xs)
      }

      override def adjoint: Gate[Seq[A]] = gate.adjoint.multi contramap (_.reverse)
    }

    def controlled[B](f: B => Universe => A): Gate[B] = new Gate[B] {
      override def apply(value: B)(universe: Universe): NonEmptyList[Universe] = {
        val result = gate(f(value)(universe))(universe)
        for (newUniverse <- result) {
          assert(f(value)(universe) == f(value)(newUniverse))
        }
        result
      }

      override def adjoint: Gate[B] = gate.adjoint controlled f
    }

    def flatMap[B](f: B => Seq[A]): Gate[B] = multi contramap f

    def filter(predicate: A => Boolean): Gate[A] = flatMap(List(_) filter predicate)
  }

}

case class Identity[A]() extends Gate[A] {
  override def apply(value: A)(universe: Universe): NonEmptyList[Universe] = NonEmptyList(universe)

  override def adjoint: Gate[A] = this
}

case object X extends Gate[StateId[Boolean]] {
  override def apply(id: StateId[Boolean])(universe: Universe): NonEmptyList[Universe] =
    NonEmptyList(universe.updatedStateWith(id)(!_))

  override def adjoint: Gate[StateId[Boolean]] = this
}

case object Z extends Gate[StateId[Boolean]] {
  override def apply(id: StateId[Boolean])(universe: Universe): NonEmptyList[Universe] =
    NonEmptyList(universe * Complex(if (universe.state(id)) -1 else 1))

  override def adjoint: Gate[StateId[Boolean]] = this
}

case object H extends Gate[StateId[Boolean]] {
  override def apply(id: StateId[Boolean])(universe: Universe): NonEmptyList[Universe] =
    NonEmptyList(
      universe / Complex((if (universe.state(id)) -1 else 1) * sqrt(2)),
      universe.updatedStateWith(id)(!_) / Complex(sqrt(2)))

  override def adjoint: Gate[StateId[Boolean]] = this
}

case object Translate extends Gate[(StateId[Vec2i], Vec2i)] {

  import Gate.GateDivisible.divisibleSyntax._

  override def apply(value: (StateId[Vec2i], Vec2i))(universe: Universe): NonEmptyList[Universe] = value match {
    case (id, delta) => NonEmptyList(universe.updatedStateWith(id)(_ + delta))
  }

  override def adjoint: Gate[(StateId[Vec2i], Vec2i)] =
    this contramap { case (id, delta) => (id, -delta) }
}
