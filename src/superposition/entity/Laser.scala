package superposition.entity

import cats.syntax.applicative.catsSyntaxApplicativeId
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import superposition.component._
import superposition.entity.Laser._
import superposition.game.ResourceResolver.resolve
import superposition.math.QExpr.QExpr
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

  val cells: Set[Vector2[Int]] = direction match {
    case Direction.Up => Set(cell + Vector2(0, 1), cell, cell + Vector2(1, 0), cell + Vector2(1, 1))
    case Direction.Right => Set(cell + Vector2(1, 0), cell, cell + Vector2(0, 1), cell + Vector2(1, 1))
    case _ => Set(cell, cell + Vector2(1, 0), cell + Vector2(0, 1), cell + Vector2(1, 1))
  }

  add(new ClassicalPosition((cell map (_.toDouble)) + Vector2(1, 1), cells))
  add(new Collider(cells.pure[QExpr]))
  add(new Beam(multiverse, gate, direction, control))
  add(new Renderable(1, control))
  add(new SpriteView(offTexture.pure[QExpr], Vector2(2.0, 2.0).pure[QExpr]))
}

/** Contains the textures for lasers. */
private object Laser {
  /** The texture for a laser. */
  private val offTexture: TextureRegion = new TextureRegion(new Texture(resolve("sprites/laser_off.png")))
}
