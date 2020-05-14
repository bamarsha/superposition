package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}

/** The renderable component allows an entity to be rendered on the screen.
  *
  * @param layer the layer to render in
  */
final class Renderable(val layer: Int = 0) extends Component

/** Contains the component mapper for the renderable component. */
object Renderable {
  /** The component mapper for the renderable component. */
  val Mapper: ComponentMapper[Renderable] = ComponentMapper.getFor(classOf[Renderable])
}
