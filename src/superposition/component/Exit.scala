package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper, Entity}
import superposition.math.Vector2

/** The goal component can require that another entity must be in the same cell as the goal. */
final class Exit extends Component

/** Contains the component mapper for the goal component. */
object Exit {
  /** The component mapper for the goal component. */
  val Mapper: ComponentMapper[Exit] = ComponentMapper.getFor(classOf[Exit])

  /** Makes an entity with the exit component at the given cells.
    *
    * @param cells the exit cells
    * @return the exit entity
    */
  def makeEntity(cells: Set[Vector2[Int]]): Entity = (new Entity)
    .add(new Exit)
    .add(new ClassicalPosition(cells = cells))
}
