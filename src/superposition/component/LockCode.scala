package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.QExpr.QExpr

final class LockCode(val bits: Seq[Boolean], val isOpen: QExpr[Boolean]) extends Component

/** Contains the component mapper for the classical position component. */
object LockCode {
  /** The component mapper for the classical position component. */
  val mapper: ComponentMapper[LockCode] = ComponentMapper.getFor(classOf[LockCode])
}
