package superposition

import engine.core.Behavior.Entity
import engine.core.Game
import engine.graphics.sprites.Sprite
import extras.physics.PositionComponent

/**
 * Contains initialization for doors.
 */
private object Door {
  private val ClosedSprite: Sprite = Sprite.load(getClass.getResource("sprites/door_closed.png"))

  private val OpenSprite: Sprite = Sprite.load(getClass.getResource("sprites/door_open.png"))

  /**
   * Declares the door system.
   */
  def declareSystem(): Unit =
    Game.declareSystem(classOf[Door], (_: Door).step())
}

/**
 * A door blocks movement unless all of the door's control cells have a bit in the on state.
 *
 * @param universe the universe this door belongs to
 * @param id       the universe object ID for this door
 * @param cell     the position of this door
 * @param controls the control cells for this door
 */
private final class Door(universe: Universe,
                         id: ObjectId,
                         cell: Cell,
                         controls: Iterable[Cell]) extends Entity with Copyable[Door] with Drawable {

  import Door._

  add(new PositionComponent(this, cell.toVec2d.add(0.5)))

  private val obj: UniverseObject = add(new UniverseObject(this, universe, id, cell, true))

  private val sprite: SpriteComponent =
    add(new SpriteComponent(this, Sprite.load(getClass.getResource("sprites/door_closed.png"))))

  override def copy(): Door = {
    val door = new Door(obj.universe, obj.id, obj.cell, controls)
    door.obj.collision = obj.collision
    door.sprite.color = sprite.color
    door.layer = layer
    door
  }

  override def draw(): Unit = sprite.draw()

  private def step(): Unit = {
    obj.collision = !controls.forall(
      obj.universe.bitsInCell(_).exists(
        obj.universe.bitMaps(_).state.get("on").contains(true)
      )
    )
    sprite.sprite = if (obj.collision) ClosedSprite else OpenSprite
  }
}
