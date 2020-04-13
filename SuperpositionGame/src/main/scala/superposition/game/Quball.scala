package superposition.game

import engine.core.Behavior.Entity
import engine.core.Game.track
import engine.graphics.sprites.Sprite
import engine.util.Color.BLACK
import engine.util.math.Vec2d
import extras.physics.PositionComponent
import superposition.types.math.Cell
import superposition.types.quantum.Id

import scala.jdk.CollectionConverters._

private object Quball {
  val All: Iterable[Quball] = track(classOf[Quball]).asScala
  private val BallSprite = Sprite.load(getClass.getResource("sprites/ball.png"))
}

/**
 * A quball is a ball that can be either on or off.
 *
 * @param multiverse the multiverse this quball belongs to
 * @param cell       the initial position of this quball
 */
private final class Quball(multiverse: Multiverse, cell: Cell) extends Entity {

  val position: PositionComponent = add(new PositionComponent(this, cell.toVec2d.add(.5)))

  val sprite: SpriteComponent = add(new SpriteComponent(this, Quball.BallSprite, new Vec2d(1, 1), BLACK))

  val qPosition: Id[Cell] = multiverse.createId(cell)
  val onOff: Id[Boolean] = multiverse.createId(false)
  val carried: Id[Boolean] = multiverse.createId(false)

  val universe: UniverseComponent = add(new UniverseComponent(this, multiverse))
  universe.primaryBit = Some(onOff)
  universe.position = Some(qPosition)
}
