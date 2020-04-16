package superposition.quantum

import scalaz.Divisible
import superposition.math.{Cell, Complex}

import scala.math.sqrt

/**
 * A quantum gate.
 *
 * @tparam A The type of the gate's argument.
 */
sealed trait Gate[A] {

  import Gate.Divisible.divisibleSyntax._

  /**
   * Applies a value to the gate within a universe.
   *
   * @param value    The value to apply.
   * @param universe The universe in which to apply the value.
   * @return The universes produced by the gate.
   */
  def apply(value: A)(universe: Universe): List[Universe]

  /**
   * The adjoint is the reverse of the gate.
   */
  def adjoint: Gate[A]

  def applyToAll(value: A)(universes: List[Universe]): List[Universe] =
    universes flatMap this(value)

  def andThen(gate: Gate[A]): Gate[A] = Divisible[Gate].divide(this, gate)(value => (value, value))

  def multi: Gate[List[A]] = new Gate[List[A]] {
    override def apply(values: List[A])(universe: Universe): List[Universe] = values match {
      case List() => List(universe)
      case x :: xs => Gate.this(x)(universe) flatMap Gate.this.multi(xs)
    }

    override def adjoint: Gate[List[A]] = Gate.this.adjoint.multi contramap (_.reverse)
  }

  def control[B](f: B => Universe => A): Gate[B] = new Gate[B] {
    override def apply(value: B)(universe: Universe): List[Universe] = {
      val result = Gate.this(f(value)(universe))(universe)
      for (newUniverse <- result) {
        assert(f(value)(universe) == f(value)(newUniverse))
      }
      result
    }

    override def adjoint: Gate[B] = Gate.this.adjoint control f
  }

  def flatMap[B](f: B => List[A]): Gate[B] = multi contramap f

  def filter(predicate: A => Boolean): Gate[A] = flatMap(List(_) filter predicate)
}

object Gate {

  implicit object Divisible extends Divisible[Gate] {
    override def conquer[A]: Gate[A] = Identity

    override def divide2[A, B, C](gate1: => Gate[A], gate2: => Gate[B])(f: C => (A, B)): Gate[C] = new Gate[C] {
      override def apply(value: C)(universe: Universe): List[Universe] = {
        val (a, b) = f(value)
        gate1(a)(universe) flatMap gate2(b)
      }

      override def adjoint: Gate[C] = divide2(gate2.adjoint, gate1.adjoint)(f(_).swap)
    }
  }

}

object Identity extends Gate[Nothing] {

  import scala.language.implicitConversions

  override def apply(value: Nothing)(universe: Universe) = List(universe)

  override def adjoint: Gate[Nothing] = this

  implicit def asGate[A](id: Identity.type): Gate[A] = id.asInstanceOf[Gate[A]]
}

object X extends Gate[StateId[Boolean]] {
  override def apply(id: StateId[Boolean])(universe: Universe): List[Universe] =
    List(universe.updatedStateWith(id)(!_))

  override def adjoint: Gate[StateId[Boolean]] = this
}

object H extends Gate[StateId[Boolean]] {
  override def apply(id: StateId[Boolean])(universe: Universe): List[Universe] = List(
    universe / Complex((if (universe.state(id)) -1 else 1) * sqrt(2)),
    universe.updatedStateWith(id)(!_) / Complex(sqrt(2))
  )

  override def adjoint: Gate[StateId[Boolean]] = this
}

object Translate extends Gate[(StateId[Cell], Int, Int)] {

  import Gate.Divisible.divisibleSyntax._

  override def apply(value: (StateId[Cell], Int, Int))(universe: Universe): List[Universe] = value match {
    case (id, x, y) => List(universe.updatedStateWith(id)(_.translate(x, y)))
  }

  override def adjoint: Gate[(StateId[Cell], Int, Int)] =
    this contramap { case (id, x, y) => (id, -x, -y) }
}
