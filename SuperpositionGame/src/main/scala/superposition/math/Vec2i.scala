package superposition.math

import engine.util.math.Vec2d

/**
 * A two-dimensional vector of integers.
 *
 * @param x the x component of this vector
 * @param y the y component of this vector
 */
final case class Vec2i(x: Int, y: Int) {
  def +(that: Vec2i): Vec2i = Vec2i(x + that.x, y + that.y)

  def unary_- : Vec2i = Vec2i(-x, -y)

  def toVec2d: Vec2d = new Vec2d(x, y)
}
