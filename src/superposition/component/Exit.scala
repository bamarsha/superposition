package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}

/** An entity with the exit tag requires that another entity must be in the same cell to finish the level. */
object Exit extends Component {
  /** The component mapper for the goal component. */
  val Mapper: ComponentMapper[_ <: Exit.type] = /*_*/ ComponentMapper.getFor(getClass) /*_*/
}
