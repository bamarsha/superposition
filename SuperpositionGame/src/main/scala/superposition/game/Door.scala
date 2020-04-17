package superposition.game

import engine.core.Behavior.Entity
import engine.graphics.sprites.Sprite
import superposition.game.Door.{ClosedSprite, OpenSprite}
import superposition.math.Vec2i

import scala.Function.const

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
private final class Door(multiverse: Multiverse, cell: Vec2i, controls: Iterable[Vec2i]) extends Entity {
  add(new SpriteComponent(this,
    sprite = universe => if (universe.allOn(controls)) OpenSprite else ClosedSprite,
    position = const(cell.toVec2d add 0.5),
    layer = -1))

  private val universe: UniverseComponent = add(new UniverseComponent(this, multiverse))
  universe.blockingCells = universe => if (universe.allOn(controls)) List() else List(cell)
}
