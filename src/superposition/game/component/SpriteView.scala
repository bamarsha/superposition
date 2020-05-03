package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import com.badlogic.gdx.graphics.Color.WHITE
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.{Color, Texture}
import superposition.math.Vector2d
import superposition.quantum.Universe

import scala.Function.const

final class SpriteView(
    val texture: Universe => Texture,
    val position: Universe => Vector2d,
    val scale: Universe => Vector2d = const(Vector2d(1, 1)),
    val color: Universe => Color = const(WHITE),
    val layer: Int = 0)
  extends Component {
  def draw(spriteBatch: SpriteBatch, universe: Universe): Unit = {
    val currentScale = scale(universe)
    val currentPosition = position(universe) - currentScale / 2
    spriteBatch.setColor(color(universe))
    spriteBatch.draw(texture(universe),
                     currentPosition.x.toFloat,
                     currentPosition.y.toFloat,
                     currentScale.x.toFloat,
                     currentScale.y.toFloat)
  }
}

object SpriteView {
  val Mapper: ComponentMapper[SpriteView] = ComponentMapper.getFor(classOf[SpriteView])
}
