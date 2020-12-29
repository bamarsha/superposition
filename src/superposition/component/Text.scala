package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.Vector2

class Text(val text: String, val pos: Vector2[Double]) extends Component

/** Contains the component mapper for the text component. */
object Text {

  /** The component mapper for the text component. */
  val mapper: ComponentMapper[Text] = ComponentMapper.getFor(classOf[Text])
}
