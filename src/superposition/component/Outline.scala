package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.math.QExpr.QExpr
import superposition.math.Vector2

/** The outline component has an outline that does not depend on which universe the entity is in.
  *
  * @param visible whether to show the outline in this universe
  * @param lowerLeft the absolute position of the outline in grid coordinates
  * @param size the size of the outline in grid coordinates
  */
final class Outline(
    val visible: QExpr[Boolean],
    val lowerLeft: Vector2[Double],
    val size: Vector2[Double] = Vector2(1, 1)
) extends Component

/** Contains the component mapper for the outline component. */
object Outline {

  /** The component mapper for the outline component. */
  val mapper: ComponentMapper[Outline] = ComponentMapper.getFor(classOf[Outline])
}
