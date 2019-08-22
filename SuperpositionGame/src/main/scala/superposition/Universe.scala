package superposition

import engine.core.Game.dt
import engine.core.Input
import engine.util.math.Vec2d

import scala.math.pow

private class Universe(size: Int) {
  var amplitude: Complex = Complex(1.0)
  var particles: Array[Particle] = Array.tabulate(size)(i => Particle(new Vec2d(i + 1.0, 1.0)))

  def state: Int =
    particles
      .zipWithIndex
      .map({ case (p, i) => if (p.on) pow(2, i).toInt else 0 })
      .sum

  def step(): Unit = {
    for (p <- particles) {
      p.position = p.position.add(p.velocity.mul(dt()))
      p.selected = Input.mouseDown(0) && (p.selected || Input.mouse().sub(p.position).length() < 0.5)
      if (p.selected) {
        p.position = p.position.lerp(Input.mouse(), dt())
      }
    }
  }

  def copy(): Universe = {
    val u = new Universe(size)
    u.amplitude = amplitude
    u.particles = particles.map(_.copy())
    u
  }
}

private case class Particle(var position: Vec2d = new Vec2d(0.0, 0.0),
                            var velocity: Vec2d = new Vec2d(0.0, 0.0),
                            var on: Boolean = false,
                            var selected: Boolean = false)