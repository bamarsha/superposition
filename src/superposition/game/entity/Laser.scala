package superposition.game.entity

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import superposition.game.ResourceResolver.resolve
import superposition.game.component.{Beam, Collision, Multiverse, Quantum, SpriteView}
import superposition.game.entity.Laser.Textures
import superposition.math.Direction.{Down, Left, Right, Up}
import superposition.math.{Direction, Vector2d, Vector2i}
import superposition.quantum.{Gate, StateId}

import scala.Function.const

/**
 * A laser applies a quantum gate to any qubit hit by its beam.
 *
 * @param multiverse the multiverse this laser belongs to
 * @param cell       the position of this laser
 * @param gate       the gate to apply
 * @param direction  the direction this laser is pointing
 * @param controls   the cell that controls this laser if it contains a bit, or None if the laser is not controlled
 */
final class Laser(
  multiverse: Multiverse,
  cell: Vector2i,
  gate: Gate[StateId[Boolean]],
  direction: Direction,
  controls: Iterable[Vector2i]) extends Entity {

  add(new SpriteView(
    texture = const(Textures(direction)),
    position = const(cell.toVector2d + Vector2d(0.5, 0.5)),
    layer = -1))

  add(new Collision(const(Set(cell))))

  add(new Beam(multiverse, gate, cell, direction, controls))

  add(new Quantum(multiverse, null))

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
}

private object Laser {
  private val Textures: Map[Direction, Texture] = Map(
    Up -> "sprites/laser_up.png",
    Down -> "sprites/laser_down.png",
    Left -> "sprites/laser_left.png",
    Right -> "sprites/laser_right.png")
      .map { case (direction, fileName) => (direction, new Texture(resolve(fileName))) }

//  private val BeamDuration: Double = 0.2
//
//  private val FadeDuration: Double = 0.3
//
//  def declareSystem(): Unit = Game.declareSystem(classOf[Laser], (_: Laser).step())
}
