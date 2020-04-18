package superposition.game

import engine.core.Behavior.Entity
import engine.core.Game._
import engine.graphics.sprites.Sprite
import engine.util.Color.{BLACK, WHITE}
import engine.util.math.Vec2d
import superposition.game.Quball.BallSprite
import superposition.math.Vec2i
import superposition.quantum.{MetaId, StateId}

import scala.Function.const
import scala.jdk.CollectionConverters._

/**
 * A quball is a ball that can be either on or off.
 *
 * @param multiverse  the multiverse this quball belongs to
 * @param initialCell the initial cell of this quball
 */
private final class Quball(multiverse: Multiverse, initialCell: Vec2i) extends Entity {
  val cell: StateId[Vec2i] = multiverse.allocate(initialCell)

  val position: MetaId[Vec2d] = multiverse.allocateMeta(initialCell.toVec2d add 0.5)

  val onOff: StateId[Boolean] = multiverse.allocate(false)

  val carried: StateId[Boolean] = multiverse.allocate(false)

  add(new SpriteComponent(this,
    sprite = const(BallSprite),
    position = _.meta(position),
    scale = const(new Vec2d(1, 1)),
    color = universe => if (universe.state(onOff)) WHITE else BLACK,
    layer = 1))

  add(new UniverseComponent(this, primaryBit = Some(onOff), position = Some(cell)))
}

private object Quball {
  val All: Iterable[Quball] = track(classOf[Quball]).asScala

  private val BallSprite = Sprite.load(getClass.getResource("sprites/ball.png"))
}
