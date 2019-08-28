package superposition

import engine.core.Behavior
import engine.core.Behavior.Component

private class Qubit extends Component {
  Behavior.track(classOf[Qubit])

  var id: Int = _

  var on: Boolean = _

  val physics: Physics = require(classOf[Physics])

  def flip(): Unit = on = !on
}
