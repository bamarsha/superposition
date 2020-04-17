package superposition.game

import java.net.URL

import engine.core.Behavior.Entity
import engine.core.Game.{dt, track}
import engine.core.{Game, Input}
import engine.graphics.Graphics._
import engine.graphics.sprites.Sprite
import engine.util.Color
import engine.util.Color._
import engine.util.math.{Transformation, Vec2d}
import superposition.game.Laser._
import superposition.math.Direction.{Down, Left, Right, Up}
import superposition.math.{Direction, Vec2i}
import superposition.quantum.{Gate, MetaId, StateId, Universe}

import scala.Function.const
import scala.jdk.CollectionConverters._

/**
 * Contains initialization for lasers.
 */
private object Laser {
  val All: Iterable[Laser] = track(classOf[Laser]).asScala

  private val Sprites: Map[Direction, URL] = Map(
    Up -> getClass.getResource("sprites/laser_up.png"),
    Down -> getClass.getResource("sprites/laser_down.png"),
    Left -> getClass.getResource("sprites/laser_left.png"),
    Right -> getClass.getResource("sprites/laser_right.png")
  )
  private val BeamDuration: Double = 0.2
  private val FadeDuration: Double = 0.3

  /**
   * Declares the laser system.
   */
  def declareSystem(): Unit = Game.declareSystem(classOf[Laser], (_: Laser).step())
}

/**
 * A laser applies a quantum gate to any qubit hit by its beam.
 *
 * @param multiverse the multiverse this laser belongs to
 * @param cell       the position of this laser
 * @param gate       the gate to apply
 * @param direction  the direction this laser is pointing
 * @param controls   the cell that controls this laser if it contains a bit, or None if the laser is not controlled
 */
private final class Laser(multiverse: Multiverse,
                          cell: Vec2i,
                          gate: Gate[StateId[Boolean]],
                          direction: Direction,
                          controls: List[Vec2i]) extends Entity {

  // Metadata
  val targetCell: MetaId[Option[Vec2i]] = multiverse.createIdMeta(None)
  val elapsedTime: MetaId[Double] = multiverse.createIdMeta(0)

  val sprite: SpriteComponent = add(new SpriteComponent(this,
    _ => Sprite.load(Laser.Sprites(direction)), _ => cell.toVec2d.add(.5)))

  val universe: UniverseComponent = add(new UniverseComponent(this, multiverse))
  universe.blockingCells = _ => List(cell)

  def draw(u: Universe): Unit = {
    if (selected()) {
      drawRectangleOutline(Transformation.create(cell.toVec2d, 0, 1), RED)
    }
    val tc = u.meta(targetCell)
    val et = u.meta(elapsedTime)
    if (tc.isDefined && et <= BeamDuration + FadeDuration) {
      drawWideLine(
        cell.toVec2d.add(.5),
        new Vec2d(tc.get.x + 0.5, tc.get.y + 0.5),
        0.25,
        new Color(1, 0, 0, math.min(FadeDuration, BeamDuration + FadeDuration - et) / FadeDuration)
      )
    }
  }

  private def beam: LazyList[Vec2i] = LazyList.iterate(cell)(_ + direction.toVec2i).tail

  private def targetCell(u: Universe): Option[Vec2i] =
    if (u.allOn(controls))
      beam.take(50) find (cell => u.isBlocked(cell) || u.allInCell(cell).nonEmpty)
    else
      None

  private def hits(u: Universe): List[StateId[Boolean]] = targetCell(u).toList.flatMap(u.getPrimaryBits)

  private val actualGate = gate.multi control const(hits)

  private def selected(): Boolean = Vec2i(Input.mouse().x.floor.toInt, Input.mouse().y.floor.toInt) == cell

  private def step(): Unit = {
    if (Input.mouseJustPressed(0) && selected()) {
      multiverse.applyGate(actualGate, ())
      multiverse.universes = multiverse.universes map (u => u.updatedMeta(targetCell)(targetCell(u)))
      multiverse.universes = multiverse.universes.map {
        u => if (targetCell(u).isEmpty) u else u.updatedMeta(elapsedTime)(0)
      }
    }
    multiverse.universes = multiverse.universes map (_.updatedMetaWith(elapsedTime)(_ + dt))
  }
}
