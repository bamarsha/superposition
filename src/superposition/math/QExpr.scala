package superposition.math

import scalaz.Monad

import scala.Function.const

/** A quantum expression.
  *
  * @param f a function that evaluates the expression within a universe
  * @tparam A the type of the expression
  */
final class QExpr[+A] private(f: Universe => A) {
  /** Evaluates the expression within a universe.
    *
    * @param universe the universe
    * @return the expression value
    */
  def apply(universe: Universe): A = f(universe)
}

/** Functions and type class instances for quantum expressions. */
object QExpr {

  /** An instance of the monad type class for quantum expressions. */
  implicit object QExprMonad extends Monad[QExpr] {
    override def bind[A, B](expr: QExpr[A])(f: A => QExpr[B]): QExpr[B] =
      new QExpr(universe => f(expr(universe))(universe))

    override def point[A](value: => A): QExpr[A] = new QExpr(const(value))
  }

  /** Returns the quantum expression for the value of the state ID.
    *
    * @param id the state ID
    * @tparam A the type of the value
    * @return the quantum expression for the value of the state ID
    */
  def apply[A](id: StateId[A]): QExpr[A] = new QExpr(_.state(id))

  /** Lifts a function into a quantum expression that would bind a value to another quantum expression.
    *
    * @param f a function that maps a value to a quantum expression
    * @tparam A the type of the argument
    * @tparam B the type of the result
    * @return the lifted function
    */
  def liftBind[A, B](f: A => QExpr[B]): QExpr[A => B] = new QExpr(universe => a => f(a)(universe))
}
