package superposition.math

/**
 * A two-dimensional vector of integers.
 *
 * @param x the x component of this vector
 * @param y the y component of this vector
 */
final case class Vector2i(x: Int, y: Int) {
  def +(that: Vector2i): Vector2i = Vector2i(x + that.x, y + that.y)

  def unary_- : Vector2i = Vector2i(-x, -y)

  def toVector2d: Vector2d = Vector2d(x, y)
}
