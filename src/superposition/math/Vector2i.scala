package superposition.math

/** A two-dimensional vector of integers.
  *
  * @param x the x component
  * @param y the y component
  */
final case class Vector2i(x: Int, y: Int) {
  /** Adds the vector to this vector.
    *
    * @param that the vector to add to this vector
    * @return the vector sum
    */
  def +(that: Vector2i): Vector2i = Vector2i(x + that.x, y + that.y)

  /** The negation of the vector. */
  def unary_- : Vector2i = Vector2i(-x, -y)

  /** The value of the vector as a [[superposition.math.Vector2d]]. */
  def toVector2d: Vector2d = Vector2d(x, y)
}
