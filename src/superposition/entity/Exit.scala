package superposition.entity

import com.badlogic.ashley.core.Entity
import superposition.component.{ClassicalPosition, Exit}
import superposition.math.Vector2

/** An entity that requires that another entity must be in the same cell to finish the level.
  *
  * @param cells the exit cells
  */
final class Exit(cells: Set[Vector2[Int]]) extends Entity {
  add(Exit)
  add(new ClassicalPosition(cells = cells))
}
