package superposition

import engine.core.Behavior.Entity
import engine.graphics.sprites.Sprite
import engine.util.Color
import engine.util.math.Vec2d
import extras.physics.{PhysicsComponent, Rectangle}

import scala.jdk.CollectionConverters._

/**
 * A quball is the most basic physical representation of a qubit.
 *
 * @param universe the universe this quball belongs to
 * @param id the universe object ID for this quball
 * @param position the initial position of this quball
 */
private class Quball(universe: Universe, id: UniversalId, position: Vec2d) extends Entity with Copyable[Quball] {
  private val physics: PhysicsComponent = addComponent(new PhysicsComponent(this))
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

  addComponent(new Drawable(
    entity = this,
    sprite = Sprite.load(getClass.getResource("sprites/ball.png")),
    color = Color.WHITE
  ))

  private val universeObject: UniverseObject = addComponent(new UniverseObject(this, universe, id))

  private val qubit: Qubit = addComponent(new Qubit(this))

  override def copy(): Quball = {
    val quball = new Quball(universeObject.universe, universeObject.id, physics.position)
    quball.qubit.on = qubit.on
    quball.physics.velocity = physics.velocity
    quball
  }
}
