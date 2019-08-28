package superposition

import engine.core.Behavior
import engine.core.Behavior.Entity
import engine.graphics.sprites.Sprite
import engine.util.Color.{BLACK, WHITE}
import engine.util.math.{Transformation, Vec2d}
import extras.physics.{PhysicsComponent, Rectangle}

import scala.jdk.CollectionConverters._

private object Quball {
  private val QuballSprite: Sprite = Sprite.load(getClass.getResource("sprites/ball.png"))
}

/**
 * A quball is a physical object that has a classical bit in each universe, but corresponds to a single qubit in the
 * multiverse.
 *
 * @param qubit the qubit number corresponding to this quball
 * @param position the initial position of this quball
 * @param on the initial state of this quball
 */
private class Quball(id: Int, on: Boolean, position: Vec2d, universe: Universe) extends Entity {
  import Quball._

  /**
   * This quball's physics component.
   */
  val physics: PhysicsComponent = require(classOf[PhysicsComponent])
  physics.position = position

  val gameObject: GameObject = require(classOf[GameObject])
  gameObject.universe = universe
  gameObject.copy = universe => new Quball(qubit.id, qubit.on, physics.position, universe).create()
  gameObject.draw = () => {
    val color = if (qubit.on) WHITE else BLACK
    QuballSprite.draw(Transformation.create(physics.position, 0, 1), color)
  }

  val qubit: Qubit = require(classOf[Qubit])
  qubit.id = id
  qubit.on = on

  override protected def onCreate(): Unit =
    physics.collider = PhysicsComponent.wallCollider(new Vec2d(1, 1), List(
      new Rectangle(new Vec2d(-8, -4.5), new Vec2d(-8, 4.5)),
      new Rectangle(new Vec2d(-8, -4.5), new Vec2d(8, -4.5)),
      new Rectangle(new Vec2d(-8, 4.5), new Vec2d(8, 4.5)),
      new Rectangle(new Vec2d(8, -4.5), new Vec2d(8, 4.5))
    ).asJavaCollection)

  /**
   * Flips this quball between the on and off states.
   */
  def flip(): Unit = qubit.on = !qubit.on
}
