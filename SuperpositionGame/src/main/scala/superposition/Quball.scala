package superposition

import engine.core.Behavior.Entity
import engine.graphics.sprites.Sprite
import engine.util.Color.{BLACK, WHITE}
import engine.util.math.Vec2d
import extras.physics.PhysicsComponent

/**
 * A quball is the most basic physical representation of a qubit.
 *
 * @param universe the universe this quball belongs to
 * @param id the universe object ID for this quball
 * @param position the initial position of this quball
 */
private class Quball(universe: Universe, id: UniversalId, position: Vec2d) extends Entity with Copyable[Quball] {
  private val universeObject: UniverseObject = add(new UniverseObject(this, universe, id, new Vec2d(1, 1)))

  private val physics: PhysicsComponent =
    add(new PhysicsComponent(this, position, new Vec2d(0, 0), universeObject.collides))

  private val drawable: Drawable = add(new Drawable(
    entity = this,
    sprite = Sprite.load(getClass.getResource("sprites/ball.png")),
    color = BLACK
  ))

  private val qubit: Qubit = add(new Qubit(this, on => drawable.color = if (on) WHITE else BLACK))

  override def copy(): Quball = {
    val quball = new Quball(universeObject.universe, universeObject.id, physics.position)
    quball.qubit.on = qubit.on
    quball.physics.velocity = physics.velocity
    quball
  }
}
