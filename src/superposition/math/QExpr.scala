package superposition.math

import cats.Monad
import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps

import scala.Function.const

/** A quantum expression.
  *
  * @param f a function that evaluates the expression within a universe
  * @tparam A the type of the expression
  */
final class QExpr[+A] private(private val f: Universe => A) extends AnyVal {
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
    override def pure[A](x: A): QExpr[A] = new QExpr(const(x))

    override def flatMap[A, B](expr: QExpr[A])(f: A => QExpr[B]): QExpr[B] =
      new QExpr(universe => f(expr(universe))(universe))

    override def tailRecM[A, B](a: A)(f: A => QExpr[Either[A, B]]): QExpr[B] = {
      @scala.annotation.tailrec
      def repeat(a: A)(universe: Universe): B = f(a)(universe) match {
        case Left(x) => repeat(x)(universe)
        case Right(y) => pure(y)(universe)
      }

      new QExpr(repeat(a))
    }
  }

  /** Operations on quantum expressions of functions.
    *
    * @param expr the function expression
    * @tparam A the function input type
    * @tparam B the function output type
    */
  implicit final class FunctionOps[A, B](expr: QExpr[A => B]) {
    /** Applies the function in the second expression to the result of the function in the first expression.
      *
      * @param next the second function expression
      * @tparam C the second function's output type
      * @return an expression of the composed functions
      */
    def andThen[C](next: QExpr[B => C]): QExpr[A => C] =
      for {
        f <- expr
        g <- next
      } yield f andThen g
  }

  /** Returns the quantum expression for the value of the state ID.
    *
    * @param id the state ID
    * @tparam A the value type
    * @return the quantum expression for the value of the state ID
    */
  def apply[A](id: StateId[A]): QExpr[A] = new QExpr(_.state(id))

  /** Prepares a function that returns a quantum expression for being applied within the context of another quantum
    * expression.
    *
    * @param f a function that maps a value to a quantum expression
    * @tparam A the input type
    * @tparam B the output type
    * @return the prepared function
    */
  def prepare[A, B](f: A => QExpr[B]): QExpr[A => B] = new QExpr(universe => a => f(a)(universe))
}
