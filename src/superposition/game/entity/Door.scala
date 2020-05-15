package superposition.game.entity

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import superposition.game.ResourceResolver.resolve
import superposition.game.component._
import superposition.game.entity.Door.{ClosedTexture, OpenTexture}
import superposition.math.Vector2
import superposition.quantum.Universe

/** A door blocks movement unless all of the door's control cells have a qubit in the on state.
  *
  * @param multiverse the multiverse the door belongs to
  * @param cell the cell position of the door
  * @param controls the control cells for the door
  */
final class Door(multiverse: Multiverse, cell: Vector2[Int], controls: Iterable[Vector2[Int]]) extends Entity {
  add(new ClassicalPosition((cell map (_.toDouble)) + Vector2(0.5, 0.5)))
  add(new Collider(universe => if (open(universe)) Set.empty else Set(cell)))
  add(new Renderable(-1))
  add(new SpriteView(universe => if (open(universe)) OpenTexture else ClosedTexture))

  /** Returns true if the door is open in the universe.
    *
    * @param universe the universe.
    * @return true if the door is open in the universe
    */
  private def open(universe: Universe): Boolean = multiverse.allOn(universe, controls)
}

/** Contains the sprite textures for doors. */
private object Door {
  /** The sprite texture for a closed door. */
  private val ClosedTexture = new Texture(resolve("sprites/door_closed.png"))

  /** The sprite texture for an open door. */
  private val OpenTexture = new Texture(resolve("sprites/door_open.png"))
}
