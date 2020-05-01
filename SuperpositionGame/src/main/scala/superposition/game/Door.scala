package superposition.game

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import superposition.game.Door.{ClosedTexture, OpenTexture}
import superposition.game.ResourceResolver.resolve
import superposition.math.{Vector2d, Vector2i}

import scala.Function.const

/**
 * A door blocks movement unless all of the door's control cells have a bit in the on state.
 *
 * @param cell     the position of this door
 * @param controls the control cells for this door
 */
private final class Door(multiverse: MultiverseComponent, cell: Vector2i, controls: Iterable[Vector2i]) extends Entity {
  add(new SpriteComponent(
    texture = universe => if (multiverse.allOn(universe, controls)) OpenTexture else ClosedTexture,
    position = const(cell.toVector2d + Vector2d(0.5, 0.5)),
    layer = -1))

  add(new UniverseComponent(
    blockingCells = universe => if (multiverse.allOn(universe, controls)) Set.empty else Set(cell)))
}

private object Door {
  private val ClosedTexture = new Texture(resolve("sprites/door_closed.png"))

  private val OpenTexture = new Texture(resolve("sprites/door_open.png"))
}
