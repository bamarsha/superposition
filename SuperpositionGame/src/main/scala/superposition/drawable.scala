package superposition

import engine.core.Behavior.{Component, Entity}
import engine.core.Game
import engine.graphics.Graphics.drawRectangleOutline
import engine.graphics.sprites.Sprite
import engine.util.Color
import engine.util.Color.{BLACK, WHITE}
import engine.util.math.{Transformation, Vec2d}
import extras.physics.PositionComponent

/**
 * An object that can be drawn.
 */
private trait Drawable {
  /**
   * The layer to which this object belongs.
   */
  var layer: Int = 0

  /**
   * Draws this object.
   */
  def draw(): Unit
}

/**
 * Adds a sprite to an entity.
 * <p>
 * Note that having this component by itself does nothing; it also needs either a [[superposition.UniverseObject]] or
 * [[superposition.Draw]] component to be drawn.
 *
 * @param entity the entity for this component
 * @param sprite the sprite for this entity
 * @param scale  the scale of the sprite
 * @param color  the color of the sprite
 */
private final class DrawableSprite(entity: Entity,
                                   var sprite: Sprite,
                                   var scale: Vec2d = new Vec2d(1, 1),
                                   var color: Color = WHITE) extends Component(entity) {
  private val position: PositionComponent = get(classOf[PositionComponent])

  private val universe: UniverseObject = get(classOf[UniverseObject])

  /**
   * Draws this sprite.
   */
  def draw(): Unit = {
    sprite.draw(Transformation.create(position.value, 0, scale), color)
    // TODO: Remove this outline.
    drawRectangleOutline(Transformation.create(universe.cell.toVec2d, 0, 1), BLACK)
  }
}

/**
 * Contains initialization for the draw component.
 */
private object Draw {
  /**
   * Declares the draw system.
   */
  def declareSystem(): Unit =
    Game.declareSystem(classOf[Draw], (_: Draw).draw())
}

/**
 * The draw component automatically draws an entity.
 *
 * @param entity the entity for this component
 */
private final class Draw(entity: Entity with Drawable) extends Component(entity) {
  private def draw(): Unit = entity.draw()
}
