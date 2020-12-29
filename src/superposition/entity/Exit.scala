package superposition.entity

import com.badlogic.ashley.core.Entity
import superposition.component
import superposition.component.ClassicalPosition
import superposition.math.Vector2

/** An entity that requires that another entity must be in the same cell to finish the level. */
object Exit {

  /** Creates an exit.
    *
    * @param cells the exit cells
    */
  def apply(cells: Set[Vector2[Int]]): Entity = {
    val entity = new Entity
    entity.add(component.Exit)
    entity.add(new ClassicalPosition(cells = cells))
    entity
  }
}
