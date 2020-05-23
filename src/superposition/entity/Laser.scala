package superposition.entity

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import superposition.component._
import superposition.entity.Laser.textures
import superposition.game.ResourceResolver.resolve
import superposition.math.Direction.{Down, Left, Right, Up}
import superposition.math.{BitSeq, Direction, Gate, StateId, Universe, Vector2}

import scala.Function.const

/** A laser applies a quantum gate to any qubit hit by its beam.
  *
  * @param multiverse the multiverse
  * @param cell the position of the laser
  * @param gate the gate that the laser applies
  * @param direction the direction the laser points
  * @param control the control function for the laser
  */
final class Laser(
    multiverse: Multiverse,
    cell: Vector2[Int],
    gate: Gate[StateId[Boolean]],
    direction: Direction,
    control: Universe => BitSeq)
  extends Entity {
  add(new ClassicalPosition((cell map (_.toDouble)) + Vector2(0.5, 0.5), Set(cell)))
  add(new Collider(const(Set(cell))))
  add(new Beam(multiverse, gate, direction, control))
  add(new Renderable(1, control))
  add(new SpriteView(texture = const(textures(direction)), scale = const(Vector2(0, 0))))
}

/** Contains the sprite textures for lasers. */
private object Laser {
  /** The sprite texture for every cardinal direction. */
  private val textures: Map[Direction, Texture] = Map(
    Up -> "sprites/laser_up.png",
    Down -> "sprites/laser_down.png",
    Left -> "sprites/laser_left.png",
    Right -> "sprites/laser_right.png")
    .map { case (direction, fileName) => (direction, new Texture(resolve(fileName))) }
}
