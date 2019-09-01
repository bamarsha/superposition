package superposition

import engine.core.Behavior.Entity
import engine.graphics.sprites.Sprite
import engine.util.Color
import engine.util.math.Vec2d
import extras.physics.PhysicsComponent

import scala.jdk.CollectionConverters._

/**
 * A quball is the most basic physical representation of a qubit.
 *
 * @param universe the universe this quball belongs to
 * @param id the universe object ID for this quball
 * @param position the initial position of this quball
 */
private class Quball(universe: Universe, id: UniversalId, position: Vec2d) extends Entity with Copyable[Quball] {
  private val physics: PhysicsComponent = add(new PhysicsComponent(
    this,
    position,
    new Vec2d(0, 0),
    PhysicsComponent.wallCollider(new Vec2d(1, 1), universe.walls.map(_.rectangle).asJavaCollection)
  ))

  add(new Drawable(
    entity = this,
    sprite = Sprite.load(getClass.getResource("sprites/ball.png")),
    color = Color.WHITE
  ))

  private val universeObject: UniverseObject = add(new UniverseObject(this, universe, id))

  private val qubit: Qubit = add(new Qubit(this))

  override def copy(): Quball = {
    val quball = new Quball(universeObject.universe, universeObject.id, physics.position)
    quball.qubit.on = qubit.on
    quball.physics.velocity = physics.velocity
    quball
  }
}
