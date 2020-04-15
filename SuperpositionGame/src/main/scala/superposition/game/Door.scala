package superposition.game

import engine.core.Behavior.Entity
import engine.graphics.sprites.Sprite
import superposition.math.Cell

/**
 * Contains initialization for doors.
 */
private object Door {
  private val ClosedSprite = Sprite.load(getClass.getResource("sprites/door_closed.png"))
  private val OpenSprite = Sprite.load(getClass.getResource("sprites/door_open.png"))
}

/**
 * A door blocks movement unless all of the door's control cells have a bit in the on state.
 *
 * @param cell     the position of this door
 * @param controls the control cells for this door
 */
private final class Door(multiverse: Multiverse, cell: Cell, controls: List[Cell]) extends Entity {

  val sprite: SpriteComponent = add(new SpriteComponent(this,
    u => if (u.allOn(controls)) Door.OpenSprite else Door.ClosedSprite, _ => cell.toVec2d.add(0.5)))
  sprite.layer = -1

  val universe: UniverseComponent = add(new UniverseComponent(this, multiverse))
  universe.blockingCells = u => if (u.allOn(controls)) List() else List(cell)
}
