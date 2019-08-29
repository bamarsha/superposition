package superposition

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
 * A quball is the most basic physical representation of a qubit.
 *
 * @param universe the universe this quball belongs to
 * @param id the qubit number corresponding to this quball
 * @param on the initial state of this quball
 * @param position the initial position of this quball
 */
private class Quball(universe: Universe, id: Int, on: Boolean, position: Vec2d) extends Entity {
  import Quball._

  private val physics: PhysicsComponent = require(classOf[PhysicsComponent])
  physics.position = position
  physics.collider = PhysicsComponent.wallCollider(
    new Vec2d(1, 1),
    List(
      new Rectangle(new Vec2d(-8, -4.5), new Vec2d(-8, 4.5)),
      new Rectangle(new Vec2d(-8, -4.5), new Vec2d(8, -4.5)),
      new Rectangle(new Vec2d(-8, 4.5), new Vec2d(8, 4.5)),
      new Rectangle(new Vec2d(8, -4.5), new Vec2d(8, 4.5))
    ).asJavaCollection
  )

  private val gameObject: GameObject = require(classOf[GameObject])
  gameObject.universe = universe
  gameObject.copyTo = copyTo
  gameObject.draw = draw

  private val qubit: Qubit = require(classOf[Qubit])
  qubit.id = id
  qubit.on = on

  private def copyTo(universe: Universe): Unit =
    new Quball(universe, qubit.id, qubit.on, physics.position).create()

  private def draw(): Unit = {
    val color = if (qubit.on) WHITE else BLACK
    QuballSprite.draw(Transformation.create(physics.position, 0, 1), color)
  }
}
