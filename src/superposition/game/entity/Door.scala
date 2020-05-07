package superposition.game.entity

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import superposition.game.ResourceResolver.resolve
import superposition.game.component._
import superposition.game.entity.Door.{ClosedTexture, OpenTexture}
import superposition.math.{Vector2d, Vector2i}

/** A door blocks movement unless all of the door's control cells have a qubit in the on state.
  *
  * @param multiverse the multiverse the door belongs to
  * @param cell the cell position of the door
  * @param controls the control cells for the door
  */
final class Door(multiverse: Multiverse, cell: Vector2i, controls: Iterable[Vector2i]) extends Entity {
  add(new Collider(universe => if (multiverse.allOn(universe, controls)) Set.empty else Set(cell)))
  add(new ClassicalPosition(cell.toVector2d + Vector2d(0.5, 0.5), cell))
  add(new SpriteView(
    texture = universe => if (multiverse.allOn(universe, controls)) OpenTexture else ClosedTexture,
    layer = -1))
}

/** Contains the sprite textures for doors. */
private object Door {
  /** The sprite texture for a closed door. */
  private val ClosedTexture = new Texture(resolve("sprites/door_closed.png"))

  /** The sprite texture for an open door. */
  private val OpenTexture = new Texture(resolve("sprites/door_open.png"))
}
