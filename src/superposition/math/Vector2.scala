package superposition.math

import com.badlogic.gdx.math.MathUtils

import scala.math.sqrt

/** A two-dimensional vector.
  *
  * @param x the x component
  * @param y the y component
  * @tparam A the type of the components
  */
final case class Vector2[A](x: A, y: A) {
  /** Maps the components of the vector.
    *
    * @param f the mapping function
    * @tparam B the new type of the components
    * @return the mapped vector
    */
  def map[B](f: A => B): Vector2[B] = Vector2(f(x), f(y))

  /** Adds two vectors.
    *
    * @param that the other vector
    * @param numeric the numeric type class
    * @return the vector sum
    */
  def +(that: Vector2[A])(implicit numeric: Numeric[A]): Vector2[A] = {
    import numeric._
    Vector2(x + that.x, y + that.y)
  }

  /** Subtracts two vectors.
    *
    * @param that the other vector
    * @param numeric the numeric type class
    * @return the vector difference
    */
  def -(that: Vector2[A])(implicit numeric: Numeric[A]): Vector2[A] = {
    import numeric._
    Vector2(x - that.x, y - that.y)
  }

  /** Computes the dot product of two vectors.
    *
    * @param that the other vector
    * @param numeric the numeric type class
    * @return the dot product
    */
  def *(that: Vector2[A])(implicit numeric: Numeric[A]): A = {
    import numeric._
    x * that.x + y * that.y
  }

  /** Multiplies the vector by a scalar.
    *
    * @param scalar the scalar
    * @param numeric the numeric type class
    * @return the scaled vector
    */
  def *(scalar: A)(implicit numeric: Numeric[A]): Vector2[A] = {
    import numeric._
    map(_ * scalar)
  }

  /** Returns the negation of the vector.
    *
    * @param numeric the numeric type class
    * @return the negation of the vector
    */
  def unary_-(implicit numeric: Numeric[A]): Vector2[A] = {
    import numeric._
    map(negate)
  }

  /** Divides the vector by a scalar.
    *
    * @param scalar the scalar
    * @return the scaled vector
    */
  def /(scalar: A)(implicit fractional: Fractional[A]): Vector2[A] = {
    import fractional._
    map(_ / scalar)
  }

  override def toString: String = s"($x, $y)"
}

/** Vector operations. */
object Vector2 {

  /** Operations on vectors of doubles.
    *
    * @param vector the vector
    */
  implicit final class DoubleOps(val vector: Vector2[Double]) extends AnyVal {
    /** The length of the vector. */
    def length: Double = sqrt(vector * vector)

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
