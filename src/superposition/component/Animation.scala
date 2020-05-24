package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.graphics.g2d.{Animation => GAnimation}
import com.badlogic.gdx.utils.{Array => GArray}
import superposition.math.QExpr.QExpr

/** The animation component adds an animated sequence of textures to an entity.
  *
  * @param frames the texture frames
  */
final class Animation(frames: Seq[QExpr[Texture]]) extends Component {
  /** The animation. */
  val animation: GAnimation[QExpr[Texture]] =
    new GAnimation(1, GArray.`with`(QExpr.toArray(frames): _*), PlayMode.LOOP)

  /** The elapsed animation time. */
  var time: Float = 0f
}

/** Contains the component mapper for animation components. */
object Animation {
  /** The component mapper for animation components. */
  val mapper: ComponentMapper[Animation] = ComponentMapper.getFor(classOf[Animation])
}
