package superposition.game

import engine.core.Behavior.{Component, Entity}
import engine.core.Game.track
import engine.graphics.sprites.Sprite
import engine.util.Color
import engine.util.Color.WHITE
import engine.util.math.{Transformation, Vec2d}
import superposition.quantum.Universe

import scala.Function.const
import scala.jdk.CollectionConverters._

/**
 * Adds a sprite to an entity.
 *
 * @param entity the entity for this component
 * @param sprite the sprite for this entity
 * @param scale  the scale of the sprite
 * @param color  the color of the sprite
 */
final class SpriteComponent(entity: Entity,
                            val sprite: Universe => Sprite,
                            val position: Universe => Vec2d,
                            val scale: Universe => Vec2d = const(new Vec2d(1, 1)),
                            val color: Universe => Color = const(WHITE),
                            val layer: Int = 0)
  extends Component(entity) {

  /**
   * Draws this sprite.
   */
  def draw(universe: Universe): Unit =
    sprite(universe).draw(
      Transformation.create(position(universe), 0, scale(universe)),
      color(universe))
}

object SpriteComponent {
  val All: Iterable[SpriteComponent] = track(classOf[SpriteComponent]).asScala
}
