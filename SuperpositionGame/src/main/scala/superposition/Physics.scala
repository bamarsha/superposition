package superposition

import java.util.function.Predicate

import engine.core.Behavior
import engine.core.Behavior.Component
import engine.util.math.Vec2d
import extras.physics.PhysicsComponent

private class Physics extends Component {
  Behavior.track(classOf[Physics])

  private val physics = require(classOf[PhysicsComponent])

  var universe: Universe = _

  var copy: Universe => Unit = _

  var destroy: () => Unit = _

  var draw: () => Unit = _

  def position: Vec2d = physics.position

  def position_=(p: Vec2d): Unit = physics.position = p

  def velocity: Vec2d = physics.velocity

  def velocity_=(v: Vec2d): Unit = physics.velocity = v

  def collider: Predicate[Vec2d] = physics.collider

  def collider_=(c: Predicate[Vec2d]): Unit = physics.collider = c

  def hitWall: Boolean = physics.hitWall

  def hitWall_=(h: Boolean): Unit = physics.hitWall = h
}
