package superposition.game

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color.WHITE
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.{Color, Texture}
import superposition.math.Vector2d
import superposition.quantum.Universe

import scala.Function.const

private final class SpriteComponent(
    val texture: Universe => Texture,
    val position: Universe => Vector2d,
    val scale: Universe => Vector2d = const(Vector2d(1, 1)),
    val color: Universe => Color = const(WHITE),
    val layer: Int = 0)
    extends Component {
  def draw(spriteBatch: SpriteBatch, universe: Universe): Unit = {
    val scale2 = scale(universe)
    val position2 = position(universe) - scale(universe) / 2
    spriteBatch.draw(texture(universe), position2.x.toFloat, position2.y.toFloat, scale2.x.toFloat, scale2.y.toFloat)
  }
}
