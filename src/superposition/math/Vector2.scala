package superposition.math

import spire.algebra.NRoot
import spire.implicits._
import spire.math.{Fractional, Numeric}

/** A two-dimensional vector.
  *
  * @param x the x component
  * @param y the y component
  * @tparam A the type of the components
  */
final case class Vector2[@specialized(Int, Double) A](x: A, y: A) {
  /** Adds two vectors.
    *
    * @param that the other vector
    * @return the vector sum
    */
  def +(that: Vector2[A])(implicit numeric: Numeric[A]): Vector2[A] = Vector2(x + that.x, y + that.y)

  /** Subtracts two vectors.
    *
    * @param that the other vector
    * @return the vector difference
    */
  def -(that: Vector2[A])(implicit numeric: Numeric[A]): Vector2[A] = Vector2(x - that.x, y - that.y)

  /** Computes the dot product of two vectors.
    *
    * @param that the other vector
    * @return the dot product
    */
  def *(that: Vector2[A])(implicit numeric: Numeric[A]): A = x * that.x + y * that.y

  /** Multiplies the vector by a scalar.
    *
    * @param scalar the scalar
    * @return the scaled vector
    */
  def *(scalar: A)(implicit numeric: Numeric[A]): Vector2[A] = Vector2(x * scalar, y * scalar)

  /** Returns the negation of the vector.
    *
    * @return the negation of the vector
    */
  def unary_-(implicit numeric: Numeric[A]): Vector2[A] = Vector2(-x, -y)

  /** Divides the vector by a scalar.
    *
    * @param scalar the scalar
    * @return the scaled vector
    */
  def /(scalar: A)(implicit fractional: Fractional[A]): Vector2[A] = Vector2(x / scalar, y / scalar)

  /** The length of the vector. */
  def length(implicit numeric: Numeric[A], nRoot: NRoot[A]): A = nRoot.sqrt(this * this)

  /** Scales the vector to have the given length.
    *
    * @param newLength the new length of the vector
    * @return the scaled vector
    */
  def withLength(newLength: A)(implicit numeric: Numeric[A], fractional: Fractional[A], nRoot: NRoot[A]): Vector2[A] =
    this / length(numeric, nRoot) * newLength

  /** Linearly interpolates this vector with the given vector.
    *
    * @param that the vector to interpolate this vector with
    * @param t a parameter between 0 and 1 such that `t` = 0 returns this vector and `t` = 1 returns that vector
    * @return the interpolated vector
    */
  def lerp(that: Vector2[A], t: A)(implicit numeric: Numeric[A]): Vector2[A] = this * (1 - t) + that * t

  /** Clamps the x and y components of this vector between the lower and upper bounds.
    *
    * @param lower the lower bound
    * @param upper the upper bound
    * @return the clamped vector
    */
  def clamp(lower: A, upper: A)(implicit numeric: Numeric[A]): Vector2[A] = {
    def clamp1(n: A) =
      if (n < lower) lower
      else if (n > upper) upper
      else n

    Vector2(clamp1(x), clamp1(y))
  }

  /** Maps the components of the vector.
    *
    * @param f the mapping function
    * @tparam B the new type of the components
    * @return the mapped vector
    */
  def map[@specialized(Int, Double) B](f: A => B): Vector2[B] = Vector2(f(x), f(y))

  override def toString: String = s"($x, $y)"
}
