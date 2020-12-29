package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}

/** The entity ID component gives an entity an identifying number.
  *
  * @param id the entity ID
  */
final class EntityId(val id: Int) extends Component

/** Contains the component mapper for the entity ID component. */
object EntityId {

  /** The component mapper for the entity ID component. */
  val mapper: ComponentMapper[EntityId] = ComponentMapper.getFor(classOf[EntityId])
}
