package superposition.game

import engine.core.Behavior.Entity
import engine.core.Game._
import engine.graphics.sprites.Sprite
import engine.util.Color.{BLACK, WHITE}
import engine.util.math.Vec2d
import superposition.math.Vec2i
import superposition.quantum.{MetaId, StateId}

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
final class Quball(multiverse: Multiverse, initialCell: Vec2i) extends Entity {

  // Quantum state
  val cell: StateId[Vec2i] = multiverse.allocate(initialCell)
  val onOff: StateId[Boolean] = multiverse.allocate(false)
  val carried: StateId[Boolean] = multiverse.allocate(false)

  // Metadata
  val position: MetaId[Vec2d] = multiverse.allocateMeta(initialCell.toVec2d.add(.5))

  val sprite: SpriteComponent = add(new SpriteComponent(this, _ => Quball.BallSprite,
    _.meta(position), _ => new Vec2d(1, 1), u => if (u.state(onOff)) WHITE else BLACK))
  sprite.layer = 1

  val universe: UniverseComponent = add(new UniverseComponent(this, multiverse))
  universe.primaryBit = Some(onOff)
  universe.position = Some(cell)
}
