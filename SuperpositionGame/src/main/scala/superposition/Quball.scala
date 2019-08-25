package superposition

import engine.core.Behavior
import engine.core.Behavior.Entity
import engine.graphics.sprites.Sprite
import engine.util.Color.{BLACK, WHITE}
import engine.util.math.{Transformation, Vec2d}
import extras.physics.{PhysicsComponent, Rectangle}

import scala.jdk.CollectionConverters._

/**
 * A quball is a physical object that has a classical bit in each universe, but corresponds to a single qubit in the
 * multiverse.
 *
 * @param position the initial position of the quball
 * @param on the initial state of the bit
 */
private class Quball(position: Vec2d, var on: Boolean = false) extends Entity {
  Behavior.track(classOf[Quball])

  /**
   * This quball's physics component.
   */
  val physics: PhysicsComponent = require(classOf[PhysicsComponent])
  physics.position = position

  override protected def onCreate(): Unit =
    physics.collider = PhysicsComponent.wallCollider(new Vec2d(1, 1), List(
      new Rectangle(new Vec2d(-8, -4.5), new Vec2d(-8, 4.5)),
      new Rectangle(new Vec2d(-8, -4.5), new Vec2d(8, -4.5)),
      new Rectangle(new Vec2d(-8, 4.5), new Vec2d(8, 4.5)),
      new Rectangle(new Vec2d(8, -4.5), new Vec2d(8, 4.5))
    ).asJavaCollection)

  /**
   * Draws this quball.
   */
  def draw(): Unit = {
    val color = if (on) WHITE else BLACK
    Sprite.load("ball.png").draw(Transformation.create(physics.position, 0, 1), color)
  }

  /**
   * Creates a copy of this quball.
   *
   * @return a copy of this quball
   */
  def copy(): Quball = {
    val quball = new Quball(physics.position, on)
    quball.physics.velocity = physics.velocity
    quball.physics.collider = physics.collider
    quball.physics.hitWall = physics.hitWall
    quball
  }
}
