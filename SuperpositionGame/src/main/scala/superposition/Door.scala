package superposition

import engine.core.Behavior.Entity
import engine.core.Game
import engine.graphics.sprites.Sprite
import extras.physics.PositionComponent

/**
 * Contains initialization for doors.
 */
private object Door {
  /**
   * Declares the door system.
   */
  def declareSystem(): Unit =
    Game.declareSystem(classOf[Door], (_: Door).step())
}

/**
 * A door blocks movement unless the door's control cell has a bit in the on state.
 *
 * @param universe the universe this door belongs to
 * @param id       the universe object ID for this door
 * @param cell     the position of this door
 * @param control  the control cell for this door
 */
private final class Door(universe: Universe,
                         id: UniversalId,
                         cell: Cell,
                         control: Cell) extends Entity with Copyable[Door] with Drawable {
  add(new PositionComponent(this, cell.toVec2d.add(0.5)))

  private val universeObject: UniverseObject = add(new UniverseObject(this, universe, id, cell, true))

  private val sprite: DrawableSprite =
    add(new DrawableSprite(this, Sprite.load(getClass.getResource("sprites/door_closed.png"))))

  override def copy(): Door = {
    val door = new Door(universeObject.universe, universeObject.id, universeObject.cell, control)
    door.universeObject.collision = universeObject.collision
    door.sprite.color = sprite.color
    door
  }

  override def draw(): Unit = sprite.draw()

  private def step(): Unit = {
    universeObject.collision = !universeObject.universe.bitsInCell(control)
      .exists(universeObject.universe.bits(_).state.get("on").contains(true))
    val filename = if (universeObject.collision) "door_closed.png" else "door_open.png"
    sprite.sprite = Sprite.load(getClass.getResource("sprites/" + filename))
  }
}
