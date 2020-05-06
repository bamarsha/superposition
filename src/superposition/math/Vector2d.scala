package superposition.math

import com.badlogic.gdx.math.MathUtils

import scala.math.sqrt

/** A two-dimensional vector of doubles.
  *
  * @param x the x component
  * @param y the y component
  */
final case class Vector2d(x: Double, y: Double) {
  /** Adds that vector to this vector.
    *
    * @param that the vector to add to this vector
    * @return the vector sum
    */
  def +(that: Vector2d): Vector2d = Vector2d(x + that.x, y + that.y)

  /** Subtracts that vector from this vector.
    *
    * @param that the vector to subtract from this vector
    * @return the vector difference
    */
  def -(that: Vector2d): Vector2d = Vector2d(x - that.x, y - that.y)

  /** Multiplies this vector by a scalar.
    *
    * @param scalar the scalar
    * @return the scaled vector
    */
  def *(scalar: Double): Vector2d = Vector2d(x * scalar, y * scalar)

  /** Divides this vector by a scalar.
    *
    * @param scalar the scalar
    * @return the scaled vector
    */
  def /(scalar: Double): Vector2d = Vector2d(x / scalar, y / scalar)

  /** The length of the vector. */
  def length: Double = sqrt(x * x + y * y)

  /** Scales the vector to have the given length.
    *
    * @param newLength the new length of the vector
    * @return the scaled vector
    */
  def withLength(newLength: Double): Vector2d = this / length * newLength

  /** Linearly interpolates this vector with the given vector.
    *
    * @param that the vector to interpolate this vector with
    * @param t a parameter between 0 and 1 such that `t` = 0 returns this vector and `t` = 1 returns that vector
    * @return the interpolated vector
    */
  def lerp(that: Vector2d, t: Double): Vector2d = this * (1 - t) + that * t

  /** Clamps the x and y components of this vector between the lower and upper bounds.
    *
    * @param lower the lower bound
    * @param upper the upper bound
    * @return the clamped vector
    */
  def clamp(lower: Double, upper: Double): Vector2d =
    Vector2d(MathUtils.clamp(x, lower, upper), MathUtils.clamp(y, lower, upper))
}
