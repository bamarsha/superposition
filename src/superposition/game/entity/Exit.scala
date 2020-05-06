package superposition.game.entity

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import superposition.game.ResourceResolver.resolve
import superposition.game.component.{ClassicalPosition, Goal, Multiverse, SpriteView}
import superposition.game.entity.Exit.KeyTexture
import superposition.math.{Vector2d, Vector2i}
import superposition.quantum.StateId

import scala.Function.const

/**
 * A goal activates an action when the required object has reached the goal in every universe.
 *
 * @param multiverse the multiverse this goal belongs to
 * @param cell       the position of this goal
 * @param required   the position of the object that must reach this goal
 */
final class Exit(
    multiverse: Multiverse,
    cell: Vector2i,
    required: () => StateId[Vector2i])
  extends Entity {
  add(new ClassicalPosition(cell.toVector2d + Vector2d(0.5, 0.5), cell))
  add(new Goal(required))
  add(new SpriteView(texture = const(KeyTexture)))
}

private object Exit {
  private val KeyTexture = new Texture(resolve("sprites/key.png"))
}
