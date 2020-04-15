package superposition.game

import java.net.URL

import engine.core.Behavior.Entity
import engine.core.Game.{dt, track}
import engine.core.{Game, Input}
import engine.graphics.sprites.Sprite
import engine.util.Color
import engine.util.Color._
import engine.util.math.{Transformation, Vec2d}
import superposition.math.{Cell, Direction}
import superposition.quantum.{Gate, Id, Universe}
import Laser._
import engine.graphics.Graphics._
import scala.jdk.CollectionConverters._


/**
 * Contains initialization for lasers.
 */
private object Laser {
  val All: Iterable[Laser] = track(classOf[Laser]).asScala

  private val Sprites: Map[Direction.Value, URL] = Map(
    Direction.Up -> getClass.getResource("sprites/laser_up.png"),
    Direction.Down -> getClass.getResource("sprites/laser_down.png"),
    Direction.Left -> getClass.getResource("sprites/laser_left.png"),
    Direction.Right -> getClass.getResource("sprites/laser_right.png")
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
                          cell: Cell,
                          gate: Gate[Id[Boolean]],
                          direction: Direction.Value,
                          controls: List[Cell]) extends Entity {

  // Metadata
  val targetCell: Id[Option[Cell]] = multiverse.createIdMeta(None)
  val elapsedTime: Id[Double] = multiverse.createIdMeta(0)

  val sprite: SpriteComponent = add(new SpriteComponent(this,
    _ => Sprite.load(Laser.Sprites(direction)), _ => cell.toVec2d.add(.5)))

  val universe: UniverseComponent = add(new UniverseComponent(this, multiverse))
  universe.blockingCells = _ => List(cell)

  def draw(u: Universe): Unit = {
    if (selected()) {
      drawRectangleOutline(Transformation.create(cell.toVec2d, 0, 1), RED)
    }
    val tc = u.getMeta(targetCell)
    val et = u.getMeta(elapsedTime)
    if (tc.isDefined && et <= BeamDuration + FadeDuration) {
      drawWideLine(
        cell.toVec2d.add(.5),
        new Vec2d(tc.get.x + 0.5, tc.get.y + 0.5),
        0.25,
        new Color(1, 0, 0, math.min(FadeDuration, BeamDuration + FadeDuration - et) / FadeDuration)
      )
    }
  }

  private def beam: LazyList[Cell] =
    LazyList.iterate(cell)(cell => cell.translate _ tupled (direction match {
      case Direction.Up => (0, 1)
      case Direction.Down => (0, -1)
      case Direction.Left => (-1, 0)
      case Direction.Right => (1, 0)
    })).tail

  private def targetCell(u: Universe): Option[Cell] =
    if (controls.forall(u.getPrimaryBits(_).exists(u.get(_))))
      beam.take(50).find(cell => u.isBlocked(cell) || u.allInCell(cell).nonEmpty)
    else None

  private def hits(u: Universe): List[Id[Boolean]] = targetCell(u).toList.flatMap(u.getPrimaryBits)

  private val actualGate = Gate.control((_: Unit) => hits)(Gate.multi(gate))

  private def selected(): Boolean = Cell(Input.mouse().x.floor.toInt, Input.mouse().y.floor.toInt) == cell

  private def step(): Unit = {
    if (Input.mouseJustPressed(0) && selected()) {
      multiverse.applyGate(actualGate, ())
      multiverse.universes = multiverse.universes.map(u => u.setMeta(targetCell)(targetCell(u)))
      multiverse.universes = multiverse.universes.map(u => if (targetCell(u).isEmpty) u else u.setMeta(elapsedTime)(0))
    }

    multiverse.universes = multiverse.universes.map(u => u.setMeta(elapsedTime)(u.getMeta(elapsedTime) + dt))
  }
}
