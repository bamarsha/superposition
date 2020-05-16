package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.{Universe, Vector2}

/** The collider component lets an entity collide with other entities.
  *
  * @param cells the set of cells that have collision
  */
final class Collider(val cells: Universe => Set[Vector2[Int]]) extends Component

/** Contains the component mapper for the collider component. */
object Collider {
  /** The component mapper for the collider component. */
  val Mapper: ComponentMapper[Collider] = ComponentMapper.getFor(classOf[Collider])
}
