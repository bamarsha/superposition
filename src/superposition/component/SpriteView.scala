package superposition.component

import cats.syntax.applicative.catsSyntaxApplicativeId
import com.badlogic.ashley.core.{Component, ComponentMapper}
import com.badlogic.gdx.graphics.Color.WHITE
import com.badlogic.gdx.graphics.Pixmap.Format.RGBA4444
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.{Color, Pixmap, Texture}
import superposition.component.SpriteView.blank
import superposition.math.QExpr.QExpr
import superposition.math.Vector2

/** The sprite view component gives an entity a renderable sprite.
  *
  * @param texture the texture that is drawn on top of the base texture
  * @param base the static base texture that does not depend on the universe
  * @param scale the sprite scale
  * @param color the sprite color
  */
final class SpriteView(
    val texture: QExpr[TextureRegion] = blank.pure[QExpr],
    val base: TextureRegion = blank,
    val scale: QExpr[Vector2[Double]] = Vector2(1.0, 1.0).pure[QExpr],
    val color: QExpr[Color] = WHITE.pure[QExpr])
  extends Component

/** Contains the component mapper for the sprite view component. */
object SpriteView {
  /** The component mapper for the sprite view component. */
  val mapper: ComponentMapper[SpriteView] = ComponentMapper.getFor(classOf[SpriteView])

  /** A blank texture. */
  val blank: TextureRegion = new TextureRegion(new Texture({
    val pixmap = new Pixmap(1, 1, RGBA4444)
    pixmap.setColor(0, 0, 0, 0)
    pixmap.fill()
    pixmap
  }))
}
