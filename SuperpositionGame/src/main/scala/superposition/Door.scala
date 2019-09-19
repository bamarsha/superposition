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
 * A door blocks movement unless all of the door's control cells have a bit in the on state.
 *
 * @param universe the universe this door belongs to
 * @param id       the universe object ID for this door
 * @param cell     the position of this door
 * @param controls the control cells for this door
 */
private final class Door(universe: Universe,
                         id: UniversalId,
                         cell: Cell,
                         controls: Iterable[Cell]) extends Entity with Copyable[Door] with Drawable {
  add(new PositionComponent(this, cell.toVec2d.add(0.5)))

  private val universeObject: UniverseObject = add(new UniverseObject(this, universe, id, cell, true))

  private val sprite: SpriteComponent =
    add(new SpriteComponent(this, Sprite.load(getClass.getResource("sprites/door_closed.png"))))

  override def copy(): Door = {
    val door = new Door(universeObject.universe, universeObject.id, universeObject.cell, controls)
    door.universeObject.collision = universeObject.collision
    door.sprite.color = sprite.color
    door.layer = layer
    door
  }

  override def draw(): Unit = sprite.draw()

  private def step(): Unit = {
    universeObject.collision = !controls.forall(
      universeObject.universe.bitsInCell(_).exists(
        universeObject.universe.bitMaps(_).state.get("on").contains(true)
      )
    )
    val url =
      if (universeObject.collision)
        getClass.getResource("sprites/door_closed.png")
      else
        getClass.getResource("sprites/door_open.png")
    sprite.sprite = Sprite.load(url)
  }
}
