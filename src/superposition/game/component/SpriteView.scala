package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import com.badlogic.gdx.graphics.Color.WHITE
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.{Color, Texture}
import superposition.math.Vector2
import superposition.quantum.Universe

import scala.Function.const

/** The sprite view component gives an entity a renderable sprite.
  *
  * @param texture the sprite texture
  * @param scale the sprite scale
  * @param color the sprite color
  */
final class SpriteView(
    val texture: Universe => Texture,
    val scale: Universe => Vector2[Double] = const(Vector2(1, 1)),
    val color: Universe => Color = const(WHITE))
  extends Component {

  /** Draws the sprite.
    *
    * @param batch the sprite batch to draw in
    * @param universe the universe to draw in
    * @param position the position of the sprite
    */
  def draw(batch: SpriteBatch, universe: Universe, position: Vector2[Double]): Unit = {
    val currentScale = scale(universe)
    val currentPosition = position - currentScale / 2
    batch.setColor(color(universe))
    batch.draw(texture(universe),
               currentPosition.x.toFloat,
               currentPosition.y.toFloat,
               currentScale.x.toFloat,
               currentScale.y.toFloat)
  }
}

/** Contains the component mapper for the sprite view component. */
object SpriteView {
  /** The component mapper for the sprite view component. */
  val Mapper: ComponentMapper[SpriteView] = ComponentMapper.getFor(classOf[SpriteView])
}
