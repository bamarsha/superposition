package superposition.game

import engine.core.Behavior.Entity
import engine.core.Game._
import engine.graphics.sprites.Sprite
import engine.util.Color.{BLACK, WHITE}
import engine.util.math.Vec2d
import superposition.math.Cell
import superposition.quantum.Id

import scala.jdk.CollectionConverters._

object Quball {
  val All: Iterable[Quball] = track(classOf[Quball]).asScala
  private val BallSprite = Sprite.load(getClass.getResource("sprites/ball.png"))
}

/**
 * A quball is a ball that can be either on or off.
 *
 * @param multiverse the multiverse this quball belongs to
 * @param initialCell       the initial position of this quball
 */
final class Quball(multiverse: Multiverse, initialCell: Cell) extends Entity {

  // Quantum state
  val cell: Id[Cell] = multiverse.createId(initialCell)
  val onOff: Id[Boolean] = multiverse.createId(false)
  val carried: Id[Boolean] = multiverse.createId(false)

  // Metadata
  val position: Id[Vec2d] = multiverse.createIdMeta(initialCell.toVec2d.add(.5))

  val sprite: SpriteComponent = add(new SpriteComponent(this, _ => Quball.BallSprite,
    _.getMeta(position), _ => new Vec2d(1, 1), u => if (u.get(onOff)) WHITE else BLACK))
  sprite.layer = 1

  val universe: UniverseComponent = add(new UniverseComponent(this, multiverse))
  universe.primaryBit = Some(onOff)
  universe.position = Some(cell)
}
