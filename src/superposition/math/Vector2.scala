package superposition.math

import com.badlogic.gdx.math.MathUtils
import scalaz.{Applicative, Traverse, Zip}

import scala.math.sqrt

/** A two-dimensional vector.
  *
  * @param x the x component
  * @param y the y component
  * @tparam A the type of the components
  */
final case class Vector2[A](x: A, y: A)

/** Vector operations and type class instances. */
object Vector2 {

  /** An instance of the [[scalaz.Traverse]] type class for [[superposition.math.Vector2]]. */
  implicit object TraverseVector2 extends Traverse[Vector2] {
    override def traverseImpl[C[_], A, B](vector: Vector2[A])(f: A => C[B])
                                         (implicit applicative: Applicative[C]): C[Vector2[B]] =
      applicative.ap2(f(vector.x), f(vector.y))(applicative pure Vector2.apply)
  }

  /** An instance of the [[scalaz.Zip]] type class for [[superposition.math.Vector2]]. */
  implicit object ZipVector2 extends Zip[Vector2] {
    override def zip[A, B](a: => Vector2[A], b: => Vector2[B]): Vector2[(A, B)] =
      Vector2((a.x, b.x), (a.y, b.y))
  }

  /** Operations on vectors of numeric values.
    *
    * @param vector the vector
    * @param numeric the numeric type class
    * @tparam A the type of the vector components
    */
  implicit final class NumericOps[A](vector: Vector2[A])(implicit numeric: Numeric[A]) {

    import TraverseVector2.traverseSyntax._
    import ZipVector2.zipSyntax._
    import numeric._

    /** Adds two vectors.
      *
      * @param that the other vector
      * @return the vector sum
      */
    def +(that: Vector2[A]): Vector2[A] = vector.fzipWith(that)(_ + _)

    /** Subtracts two vectors.
      *
      * @param that the other vector
      * @return the vector difference
      */
    def -(that: Vector2[A]): Vector2[A] = vector.fzipWith(that)(_ - _)

    /** Multiplies the vector by a scalar.
      *
      * @param scalar the scalar
      * @return the scaled vector
      */
    def *(scalar: A): Vector2[A] = vector map (_ * scalar)

    /** The negation of the vector. */
    def unary_- : Vector2[A] = vector map negate
  }

  /** Operations on vectors of fractional values.
    *
    * @param vector the vector
    * @param fractional the fractional type class
    * @tparam A the type of the vector components
    */
  implicit final class FractionalOps[A](vector: Vector2[A])(implicit fractional: Fractional[A]) {

    import TraverseVector2.traverseSyntax._
    import fractional._

    /** Divides the vector by a scalar.
      *
      * @param scalar the scalar
      * @return the scaled vector
      */
    def /(scalar: A): Vector2[A] = vector map (_ / scalar)
  }

  /** Operations on vectors of doubles.
    *
    * @param vector the vector
    */
  implicit final class DoubleOps(vector: Vector2[Double]) {

    import TraverseVector2.traverseSyntax._

    /** The length of the vector. */
    def length: Double = sqrt(vector.x * vector.x + vector.y * vector.y)

    /** Scales the vector to have the given length.
      *
      * @param newLength the new length of the vector
      * @return the scaled vector
      */
    def withLength(newLength: Double): Vector2[Double] = vector / length * newLength

    /** Linearly interpolates this vector with the given vector.
      *
      * @param that the vector to interpolate this vector with
      * @param t a parameter between 0 and 1 such that `t` = 0 returns this vector and `t` = 1 returns that vector
      * @return the interpolated vector
      */
    def lerp(that: Vector2[Double], t: Double): Vector2[Double] = vector * (1 - t) + that * t

    /** Clamps the x and y components of this vector between the lower and upper bounds.
      *
      * @param lower the lower bound
      * @param upper the upper bound
      * @return the clamped vector
      */
    def clamp(lower: Double, upper: Double): Vector2[Double] = vector map (MathUtils.clamp(_, lower, upper))
  }

}
