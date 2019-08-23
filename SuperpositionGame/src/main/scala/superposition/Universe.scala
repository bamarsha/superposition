package superposition

import engine.util.math.Vec2d

import scala.math.pow

private class Universe(size: Int) {
  var amplitude: Complex = Complex(1.0)
  var bits: Array[Bit] = Array.tabulate(size)(i => Bit(new Vec2d(i + 1.0, 1.0)))

  def state: Int =
    bits.zipWithIndex.map { case (p, i) => if (p.on) pow(2, i).toInt else 0 }.sum

  def step(): Unit = bits.foreach(_.step())

  def copy(): Universe = {
    val u = new Universe(size)
    u.amplitude = amplitude
    u.bits = bits.map(_.copy())
    u
  }
}
