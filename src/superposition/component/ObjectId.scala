package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}

final class ObjectId(val id: Int) extends Component

object ObjectId {
  val mapper: ComponentMapper[ObjectId] = ComponentMapper.getFor(classOf[ObjectId])
}
