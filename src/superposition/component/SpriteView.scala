package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import com.badlogic.gdx.graphics.Color.WHITE
import com.badlogic.gdx.graphics.{Color, Texture}
import scalaz.syntax.monad._
import superposition.math.{QExpr, Vector2}

/** The sprite view component gives an entity a renderable sprite.
  *
  * @param texture the sprite texture
  * @param scale the sprite scale
  * @param color the sprite color
  */
final class SpriteView(
    val texture: QExpr[Texture],
    val scale: QExpr[Vector2[Double]] = Vector2(1.0, 1.0).pure[QExpr],
    val color: QExpr[Color] = WHITE.pure[QExpr])
  extends Component

/** Contains the component mapper for the sprite view component. */
object SpriteView {
  /** The component mapper for the sprite view component. */
  val mapper: ComponentMapper[SpriteView] = ComponentMapper.getFor(classOf[SpriteView])
}
