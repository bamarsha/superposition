package superposition.math

import cats.Monad
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.estatico.newtype.macros.newtype
import io.estatico.newtype.ops._

import scala.Function.const
import scala.collection.mutable
import scala.language.implicitConversions

/** Quantum expressions. */
object QExpr {

  /** A quantum expression.
    *
    * @param f a function that evaluates the expression within a universe
    * @tparam A the expression type
    */
  @newtype final class QExpr[+A] private (val f: Universe => A) {

    /** Evaluates the expression within a universe.
      *
      * @param universe the universe
      * @return the expression value
      */
    def apply(universe: Universe): A = f(universe)

    /** Memoizes the result of the expression based on the quantum state of the universe. */
    def memoized: QExpr[A] = {
      val cache = new mutable.WeakHashMap[DependentMap[StateId[_]], A]
      QExpr { universe =>
        cache.underlying.get(universe.state) match {
          case null =>
            val result = f(universe)
            cache(universe.state) = result
            result
          case result => result
        }
      }
    }
  }

  /** Functions and type class instances for quantum expressions. */
  object QExpr {

    /** An instance of the monad type class for quantum expressions. */
    implicit object QExprMonad extends Monad[QExpr] {
      override def pure[A](x: A): QExpr[A] = QExpr(const(x))

      override def map[A, B](expr: QExpr[A])(f: A => B): QExpr[B] = QExpr(universe => f(expr(universe)))

      override def map2[A, B, C](expr1: QExpr[A], expr2: QExpr[B])(f: (A, B) => C): QExpr[C] =
        QExpr(universe => f(expr1(universe), expr2(universe)))

      override def map3[A, B, C, D](expr1: QExpr[A], expr2: QExpr[B], expr3: QExpr[C])(f: (A, B, C) => D): QExpr[D] =
        QExpr(universe => f(expr1(universe), expr2(universe), expr3(universe)))

      override def flatMap[A, B](expr: QExpr[A])(f: A => QExpr[B]): QExpr[B] =
        QExpr(universe => f(expr(universe))(universe))

      override def tailRecM[A, B](a: A)(f: A => QExpr[Either[A, B]]): QExpr[B] = {
        @scala.annotation.tailrec
        def repeat(a: A)(universe: Universe): B = f(a)(universe) match {
          case Left(x) => repeat(x)(universe)
          case Right(x) => pure(x)(universe)
        }

        QExpr(repeat(a))
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

    /** Makes a quantum expression.
      *
      * @param f a function that evaluates the expression within a universe
      * @tparam A the expression type
      * @return the quantum expression
      */
    private def apply[A](f: Universe => A) = f.coerce[QExpr[A]]

    /** Returns the quantum expression for the value of the state ID.
      *
      * @param id the state ID
      * @tparam A the value type
      * @return the quantum expression for the value of the state ID
      */
    def ofState[A](id: StateId[A]): QExpr[A] = QExpr(_.state(id))

    /** Returns the quantum expression for the value of the metadata ID.
      *
      * @param id the metadata ID
      * @tparam A the value type
      * @return the quantum expression for the value of the metadata ID
      */
    def ofMeta[A](id: MetaId[A]): QExpr[A] = QExpr(_.meta(id))

    /** Prepares a function that returns a quantum expression for being applied within the context of another quantum
      * expression.
      *
      * @param f a function that maps a value to a quantum expression
      * @tparam A the input type
      * @tparam B the output type
      * @return the prepared function
      */
    def prepare[A, B](f: A => QExpr[B]): QExpr[A => B] = QExpr(universe => a => f(a)(universe))
  }
}
