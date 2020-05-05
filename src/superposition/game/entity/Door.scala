package superposition.game.entity

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import superposition.game.ResourceResolver.resolve
import superposition.game.component.{Collider, Multiverse, ClassicalPosition, SpriteView}
import superposition.game.entity.Door.{ClosedTexture, OpenTexture}
import superposition.math.{Vector2d, Vector2i}

import scala.Function.const

/**
 * A door blocks movement unless all of the door's control cells have a bit in the on state.
 *
 * @param multiverse the multiverse the door belongs to
 * @param cell       the position of the door
 * @param controls   the control cells for the door
 */
final class Door(multiverse: Multiverse, cell: Vector2i, controls: Iterable[Vector2i]) extends Entity {
  add(new Collider(universe => if (multiverse.allOn(universe, controls)) Set.empty else Set(cell)))
  add(new ClassicalPosition(cell.toVector2d + Vector2d(0.5, 0.5), cell))
  add(new SpriteView(
    texture = universe => if (multiverse.allOn(universe, controls)) OpenTexture else ClosedTexture,
    layer = -1))
}

private object Door {
  private val ClosedTexture = new Texture(resolve("sprites/door_closed.png"))

  private val OpenTexture = new Texture(resolve("sprites/door_open.png"))
}
