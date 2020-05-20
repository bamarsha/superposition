package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}

class ObjectId(val id: Int) extends Component { }

/** Contains the component mapper for the quantum position component. */
object ObjectId {
  /** The component mapper for the quantum position component. */
  val Mapper: ComponentMapper[ObjectId] = ComponentMapper.getFor(classOf[ObjectId])
}
