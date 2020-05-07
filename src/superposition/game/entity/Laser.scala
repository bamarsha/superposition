package superposition.game.entity

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import superposition.game.ResourceResolver.resolve
import superposition.game.component._
import superposition.game.entity.Laser.Textures
import superposition.math.Direction.{Down, Left, Right, Up}
import superposition.math.{Direction, Vector2d, Vector2i}
import superposition.quantum.{Gate, StateId}

import scala.Function.const

/** A laser applies a quantum gate to any qubit hit by its beam.
  *
  * @param multiverse the multiverse
  * @param cell the position of the laser
  * @param gate the gate that the laser applies
  * @param direction the direction the laser points
  * @param controls the control cells for the laser
  */
final class Laser(
    multiverse: Multiverse,
    cell: Vector2i,
    gate: Gate[StateId[Boolean]],
    direction: Direction,
    controls: Iterable[Vector2i])
  extends Entity {
  add(new ClassicalPosition(cell.toVector2d + Vector2d(0.5, 0.5), cell))
  add(new Collider(const(Set(cell))))
  add(new Beam(multiverse, gate, direction, controls))
  add(new SpriteView(texture = const(Textures(direction)), layer = -1))
}

/** Contains the sprite textures for lasers. */
private object Laser {
  /** The sprite texture for every cardinal direction. */
  private val Textures: Map[Direction, Texture] = Map(
    Up -> "sprites/laser_up.png",
    Down -> "sprites/laser_down.png",
    Left -> "sprites/laser_left.png",
    Right -> "sprites/laser_right.png")
    .map { case (direction, fileName) => (direction, new Texture(resolve(fileName))) }
}
