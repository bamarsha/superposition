package superposition.game.entity

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import superposition.game.ResourceResolver.resolve
import superposition.game.component.{Beam, ClassicalPosition, Collider, Multiverse, QuantumObject, SpriteView}
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
    controls: Iterable[Vector2i])
  extends Entity {
  add(new QuantumObject(multiverse))
  add(new ClassicalPosition(cell.toVector2d + Vector2d(0.5, 0.5), cell))
  add(new Collider(const(Set(cell))))
  add(new Beam(multiverse, gate, direction, controls))
  add(new SpriteView(texture = const(Textures(direction)), layer = -1))
}

private object Laser {
  private val Textures: Map[Direction, Texture] = Map(
    Up -> "sprites/laser_up.png",
    Down -> "sprites/laser_down.png",
    Left -> "sprites/laser_left.png",
    Right -> "sprites/laser_right.png")
    .map { case (direction, fileName) => (direction, new Texture(resolve(fileName))) }
}
