package superposition.game

import engine.core.Behavior.{Component, Entity}
import engine.core.Game.track
import engine.graphics.sprites.Sprite
import engine.util.Color
import engine.util.Color.WHITE
import engine.util.math.{Transformation, Vec2d}
import superposition.types.quantum.Universe

import scala.jdk.CollectionConverters._

object SpriteComponent {
  val All: Iterable[SpriteComponent] = track(classOf[SpriteComponent]).asScala
}

/**
 * Adds a sprite to an entity.
 * <p>
 * Note that having this component by itself does nothing; it also needs a [[UniverseObject]] component to
 * be drawn.
 *
 * @param entity the entity for this component
 * @param sprite the sprite for this entity
 * @param scale  the scale of the sprite
 * @param color  the color of the sprite
 */
final class SpriteComponent(entity: Entity,
                            val sprite: Universe => Sprite,
                            val position: Universe => Vec2d,
                            val scale: Universe => Vec2d = _ => new Vec2d(1, 1),
                            val color: Universe => Color = _ => WHITE,
                            var layer: Int = 0) extends Component(entity) {

  /**
   * Draws this sprite.
   */
  def draw(u: Universe): Unit = sprite(u).draw(Transformation.create(position(u), 0, scale(u)), color(u))
}
