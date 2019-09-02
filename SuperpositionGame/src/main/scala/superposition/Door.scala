package superposition

import engine.core.Behavior.Entity
import engine.graphics.sprites.Sprite
import engine.util.Color.{BLACK, WHITE}
import engine.util.math.Vec2d
import extras.physics.PhysicsComponent

/**
 * A door is an object with a qubit that other objects can pass through if the qubit is off, but not if the qubit is on.
 *
 * @param universe the universe this door belongs to
 * @param id       the universe object ID for this door
 * @param position the position of this door
 */
private final class Door(universe: Universe, id: UniversalId, position: Vec2d) extends Entity with Copyable[Door] {
  private val universeObject: UniverseObject = add(new UniverseObject(this, universe, id, new Vec2d(1, 1)))

  private val physics: PhysicsComponent =
    add(new PhysicsComponent(this, position, new Vec2d(0, 0), universeObject.collides))

  private val drawable: Drawable =
    add(new Drawable(this, Sprite.load(getClass.getResource("sprites/roof_right.png")), color = BLACK))

  private val qubit: Qubit = add(new Qubit(this, on => {
    universeObject.collidesWithObjects = on
    drawable.color = if (on) WHITE else BLACK
  }))

  override def copy(): Door = {
    val door = new Door(universeObject.universe, universeObject.id, physics.position)
    door.qubit.on = qubit.on
    door
  }
}
