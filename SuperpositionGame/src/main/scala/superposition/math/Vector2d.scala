package superposition.math

final case class Vector2d(x: Double, y: Double) {
  def +(that: Vector2d): Vector2d = Vector2d(x + that.x, y + that.y)

  def -(that: Vector2d): Vector2d = Vector2d(x - that.x, y - that.y)

  def /(scalar: Double): Vector2d = Vector2d(x / scalar, y / scalar)
}
