package superposition.quantum

import superposition.math.{Cell, Complex}

import scala.math.sqrt

/**
 * A quantum gate.
 *
 * @tparam A The type of the gate's argument.
 */
sealed trait Gate[A] {
  /**
   * Applies a value to the gate within a universe.
   * @param value The value to apply.
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

  def andThen(gate: Gate[A]): Gate[A] = divide2(gate)(value => (value, value))

  def contramap[B](f: B => A): Gate[B] = new Gate[B] {
    override def apply(b: B)(universe: Universe): List[Universe] = Gate.this(f(b))(universe)

    override def adjoint: Gate[B] = Gate.this.adjoint contramap f
  }

  def divide2[B, C](gate: Gate[B])(f: C => (A, B)): Gate[C] = new Gate[C] {
    override def apply(value: C)(universe: Universe): List[Universe] = {
      val (a, b) = f(value)
      Gate.this(a)(universe) flatMap gate(b)
    }

    override def adjoint: Gate[C] = /*_*/ gate.adjoint.divide2(Gate.this.adjoint)(f(_).swap) /*_*/
  }

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

object X extends Gate[Id[Boolean]] {
  override def apply(id: Id[Boolean])(universe: Universe): List[Universe] =
    List(universe.set(id)(!universe.get(id)))

  override def adjoint: Gate[Id[Boolean]] = this
}

object H extends Gate[Id[Boolean]] {
  override def apply(id: Id[Boolean])(universe: Universe): List[Universe] = List(
    universe / Complex((if (universe.get(id)) -1 else 1) * sqrt(2)),
    universe.set(id)(!universe.get(id)) / Complex(sqrt(2))
  )

  override def adjoint: Gate[Id[Boolean]] = this
}

object Translate extends Gate[(Id[Cell], Int, Int)] {
  override def apply(value: (Id[Cell], Int, Int))(universe: Universe): List[Universe] = value match {
    case (id, x, y) => List(universe.set(id)(universe.get(id).translate(x, y)))
  }

  override def adjoint: Gate[(Id[Cell], Int, Int)] =
    this contramap { case (id, x, y) => (id, -x, -y) }
}
