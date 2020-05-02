package superposition.math

import scala.math.sqrt

final case class Vector2d(x: Double, y: Double) {
  def +(that: Vector2d): Vector2d = Vector2d(x + that.x, y + that.y)

  def -(that: Vector2d): Vector2d = Vector2d(x - that.x, y - that.y)

  def *(scalar: Double): Vector2d = Vector2d(x * scalar, y * scalar)

  def /(scalar: Double): Vector2d = Vector2d(x / scalar, y / scalar)

  def length: Double = sqrt(x * x + y * y)

  def withLength(newLength: Double): Vector2d = this / length * newLength

  def lerp(that: Vector2d, amount: Double): Vector2d = this * (1 - amount) + that * amount
}
