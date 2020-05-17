package superposition.math

import com.badlogic.gdx.math.MathUtils

import scala.math.sqrt

/** A two-dimensional vector.
  *
  * @param x the x component
  * @param y the y component
  * @tparam A the type of the components
  */
final case class Vector2[@specialized(Int, Double) A](x: A, y: A) {
  /** Maps the components of the vector.
    *
    * @param f the mapping function
    * @tparam B the new type of the components
    * @return the mapped vector
    */
  def map[@specialized(Int, Double) B](f: A => B): Vector2[B] = Vector2(f(x), f(y))

  override def toString: String = s"($x, $y)"
}

/** Vector operations. */
object Vector2 {

  /** Operations on vectors of integers.
    *
    * @param vector the vector
    */
  implicit final class IntOps(val vector: Vector2[Int]) extends AnyVal {
    /** Adds two vectors.
      *
      * @param that the other vector
      * @return the vector sum
      */
    def +(that: Vector2[Int]): Vector2[Int] = Vector2(vector.x + that.x, vector.y + that.y)

    /** Subtracts two vectors.
      *
      * @param that the other vector
      * @return the vector difference
      */
    def -(that: Vector2[Int]): Vector2[Int] = Vector2(vector.x - that.x, vector.y - that.y)

    /** Computes the dot product of two vectors.
      *
      * @param that the other vector
      * @return the dot product
      */
    def *(that: Vector2[Int]): Int = vector.x * that.x + vector.y * that.y

    /** Multiplies the vector by a scalar.
      *
      * @param scalar the scalar
      * @return the scaled vector
      */
    def *(scalar: Int): Vector2[Int] = vector map (_ * scalar)

    /** Returns the negation of the vector.
      *
      * @return the negation of the vector
      */
    def unary_- : Vector2[Int] = vector map (-_)
  }

  /** Operations on vectors of doubles.
    *
    * @param vector the vector
    */
  implicit final class DoubleOps(val vector: Vector2[Double]) extends AnyVal {
    /** Adds two vectors.
      *
      * @param that the other vector
      * @return the vector sum
      */
    def +(that: Vector2[Double]): Vector2[Double] = Vector2(vector.x + that.x, vector.y + that.y)

    /** Subtracts two vectors.
      *
      * @param that the other vector
      * @return the vector difference
      */
    def -(that: Vector2[Double]): Vector2[Double] = Vector2(vector.x - that.x, vector.y - that.y)

    /** Computes the dot product of two vectors.
      *
      * @param that the other vector
      * @return the dot product
      */
    def *(that: Vector2[Double]): Double = vector.x * that.x + vector.y * that.y

    /** Multiplies the vector by a scalar.
      *
      * @param scalar the scalar
      * @return the scaled vector
      */
    def *(scalar: Double): Vector2[Double] = vector map (_ * scalar)

    /** Divides the vector by a scalar.
      *
      * @param scalar the scalar
      * @return the scaled vector
      */
    def /(scalar: Double): Vector2[Double] = vector map (_ / scalar)

    /** Returns the negation of the vector.
      *
      * @return the negation of the vector
      */
    def unary_- : Vector2[Double] = vector map (-_)

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
