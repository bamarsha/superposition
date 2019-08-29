package superposition

import engine.core.Behavior
import engine.core.Behavior.Component

private class Qubit extends Component {
  Behavior.track(classOf[Qubit])

  val gameObject: GameObject = require(classOf[GameObject])

  var id: Int = _

  var on: Boolean = _

  override protected def onCreate(): Unit = gameObject.universe.add(this)

  def flip(): Unit = on = !on
}
