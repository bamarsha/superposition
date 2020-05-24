package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.QExpr.QExpr

/** The renderable component allows an entity to be rendered on the screen.
  *
  * @param layer the layer to render in
  * @param dependentState the quantum state that the renderers depend on
  */
final class Renderable(val layer: Int, val dependentState: QExpr[Any]) extends Component

/** Contains the component mapper for the renderable component. */
object Renderable {
  /** The component mapper for the renderable component. */
  val mapper: ComponentMapper[Renderable] = ComponentMapper.getFor(classOf[Renderable])
}
