package superposition.entity

import cats.syntax.applicative.catsSyntaxApplicativeId
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import superposition.component._
import superposition.entity.Laser.textures
import superposition.game.ResourceResolver.resolve
import superposition.math.Direction.{Down, Left, Right, Up}
import superposition.math._

/** A laser applies a quantum gate to any qubit hit by its beam.
  *
  * @param multiverse the multiverse
  * @param cell the position of the laser
  * @param gate the gate that the laser applies
  * @param direction the direction the laser points
  * @param control the control for the laser
  */
final class Laser(
    multiverse: Multiverse,
    cell: Vector2[Int],
    gate: Gate[StateId[Boolean]],
    direction: Direction,
    control: QExpr[BitSeq])
  extends Entity {
  add(new ClassicalPosition((cell map (_.toDouble)) + Vector2(0.5, 0.5), Set(cell)))
  add(new Collider(Set(cell).pure[QExpr]))
  add(new Beam(multiverse, gate, direction, control))
  add(new Renderable(1, control))
  add(new SpriteView(texture = textures(direction).pure[QExpr], scale = Vector2(0.0, 0.0).pure[QExpr]))
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
