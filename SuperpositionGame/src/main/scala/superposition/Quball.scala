package superposition

import engine.core.Behavior.Entity
import engine.graphics.sprites.Sprite
import engine.util.math.Vec2d
import extras.physics.{PhysicsComponent, Rectangle}

import scala.jdk.CollectionConverters._

/**
 * A quball is the most basic physical representation of a qubit.
 *
 * @param universe the universe this quball belongs to
 * @param id the universe object ID for this quball
 * @param on the initial state of this quball
 * @param position the initial position of this quball
 */
private class Quball(universe: Universe, id: UniversalId, on: Boolean, position: Vec2d) extends Entity {
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

  private val drawable: Drawable = require(classOf[Drawable])
  drawable.sprite = Sprite.load(getClass.getResource("sprites/ball.png"))

  private val universeObject: UniverseObject = require(classOf[UniverseObject])
  universeObject.id = id
  universeObject.universe = universe
  universeObject.copyTo = copyTo

  private val qubit: Qubit = require(classOf[Qubit])
  qubit.on = on

  private def copyTo(universe: Universe): Entity = {
    val quball = new Quball(universe, qubit.universeObject.id, qubit.on, physics.position)
    quball.physics.velocity = physics.velocity
    quball
  }
}
