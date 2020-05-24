package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}

final class LockCode(val bits: Seq[Boolean]) extends Component

/** Contains the component mapper for the classical position component. */
object LockCode {
  /** The component mapper for the classical position component. */
  val mapper: ComponentMapper[LockCode] = ComponentMapper.getFor(classOf[LockCode])
}
