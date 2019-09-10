package superposition

import engine.core.Behavior.Entity
import engine.graphics.sprites.Sprite
import engine.util.Color.{BLACK, WHITE}
import engine.util.math.Vec2d
import extras.physics.PositionComponent

/**
 * A quball is a ball that can be either on or off.
 *
 * @param universe the universe this quball belongs to
 * @param id       the universe object ID for this quball
 * @param cell     the initial position of this quball
 */
private final class Quball(universe: Universe,
                           id: UniversalId,
                           cell: Cell) extends Entity with Copyable[Quball] with Drawable {
  add(new PositionComponent(this, new Vec2d(cell.column + 0.5, cell.row + 0.5)))

  private val universeObject: UniverseObject = add(new UniverseObject(this, universe, id, cell))

  private val sprite: DrawableSprite = add(new DrawableSprite(
    entity = this,
    sprite = Sprite.load(getClass.getResource("sprites/ball.png")),
    color = BLACK
  ))

  private val bits: BitMap = add(new BitMap(
    this,
    Map("on" -> false, "carried" -> false),
    "on",
    state => sprite.color = if (state("on")) WHITE else BLACK
  ))

  override def copy(): Quball = {
    val quball = new Quball(universeObject.universe, universeObject.id, universeObject.cell)
    quball.bits.state = bits.state
    quball
  }

  override def draw(): Unit = sprite.draw()
}
