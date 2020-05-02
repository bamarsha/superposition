package superposition.game

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import superposition.game.Laser.Textures
import superposition.game.ResourceResolver.resolve
import superposition.math.Direction.{Down, Left, Right, Up}
import superposition.math.{Direction, Vector2d, Vector2i}
import superposition.quantum.{Gate, StateId}

import scala.Function.const

//import java.net.URL
//
//import engine.core.Behavior.Entity
//import engine.core.Game
//import engine.core.Game.{dt, track}
//import engine.core.Input.{mouse, mouseJustPressed}
//import engine.graphics.Graphics._
//import engine.graphics.sprites.Sprite
//import engine.util.Color
//import engine.util.Color._
//import engine.util.math.Transformation
//import superposition.game.GameUniverse.Ops
//import superposition.game.Laser._
//import superposition.math.Direction.{Down, Left, Right, Up}
//import superposition.math.{Direction, Vec2i}
//import superposition.quantum.{Gate, MetaId, StateId, Universe}
//
//import scala.Function.const
//import scala.jdk.CollectionConverters._
//import scala.math.min

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
                          cell: Vector2i,
                          gate: Gate[StateId[Boolean]],
                          direction: Direction,
                          controls: Iterable[Vector2i]) extends Entity {
//  private val lastTarget: MetaId[Option[Vec2i]] = multiverse.allocateMeta(None)
//
//  private val elapsedTime: MetaId[Double] = multiverse.allocateMeta(0)

  add(new SpriteView(
    texture = const(Textures(direction)),
    position = const(cell.toVector2d + Vector2d(0.5, 0.5))))

  add(new BasicState(blockingCells = const(Set(cell))))

//  private val beam: LazyList[Vec2i] = LazyList.iterate(cell)(_ + direction.toVec2i).tail
//
//  /**
//   * Draws the laser in a universe.
//   *
//   * @param universe the universe to draw in
//   */
//  def draw(universe: Universe): Unit = {
//    if (isSelected(cell)) {
//      drawRectangleOutline(Transformation.create(cell.toVec2d, 0, 1), RED)
//    }
//    universe.meta(lastTarget) match {
//      case Some(target) if universe.meta(elapsedTime) <= BeamDuration + FadeDuration =>
//        drawWideLine(
//          cell.toVec2d add 0.5,
//          target.toVec2d add 0.5,
//          0.25,
//          new Color(1, 0, 0,
//            min(FadeDuration, BeamDuration + FadeDuration - universe.meta(elapsedTime)) / FadeDuration))
//      case _ => ()
//    }
//  }
//
//  private def target(universe: Universe): Option[Vec2i] =
//    if (universe.allOn(controls))
//      beam.take(BeamLength) find (cell => universe.isBlocked(cell) || universe.allInCell(cell).nonEmpty)
//    else None
//
//  private def hits(universe: Universe): Seq[StateId[Boolean]] =
//    target(universe).iterator.to(Seq) flatMap universe.primaryBits
//
//  private def step(): Unit = {
//    if (mouseJustPressed(0) && isSelected(cell)) {
//      multiverse.applyGate(gate.multi controlled const(hits), ())
//      multiverse.updateMetaWith(lastTarget)(const(target))
//      multiverse.updateMetaWith(elapsedTime) { time => universe =>
//        if (target(universe).isEmpty) time else 0
//      }
//    }
//    multiverse.updateMetaWith(elapsedTime)(time => const(time + dt))
//  }
}

private object Laser {
//  val All: Iterable[Laser] = track(classOf[Laser]).asScala

  private val Textures: Map[Direction, Texture] = Map(
    Up -> "sprites/laser_up.png",
    Down -> "sprites/laser_down.png",
    Left -> "sprites/laser_left.png",
    Right -> "sprites/laser_right.png")
      .map { case (direction, fileName) => (direction, new Texture(resolve(fileName))) }

//  private val BeamLength: Int = 25
//
//  private val BeamDuration: Double = 0.2
//
//  private val FadeDuration: Double = 0.3
//
//  def declareSystem(): Unit = Game.declareSystem(classOf[Laser], (_: Laser).step())
//
//  private def isSelected(cell: Vec2i): Boolean =
//    cell == Vec2i(mouse.x.floor.toInt, mouse.y.floor.toInt)
}
