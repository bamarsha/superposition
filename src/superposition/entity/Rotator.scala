package superposition.entity

import cats.implicits._
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import superposition.component._
import superposition.entity.Rotator._
import superposition.game.ResourceResolver.resolve
import superposition.math.QExpr.QExpr
import superposition.math._

/** A laser applies a quantum gate to any qubit hit by its beam.
  *
  * @param multiverse the multiverse
  * @param cell the position of the laser
  */
final class Rotator(
    multiverse: Multiverse,
    cell: Vector2[Int],
    control1: QExpr[BitSeq],
    control2: QExpr[BitSeq])
  extends Entity {
  locally {
    val cells = Set(cell, cell + Vector2(1, 0), cell + Vector2(0, 1), cell + Vector2(1, 1))
    val texture = control1 map (bits => if (bits.any) onTexture else offTexture)
    val unitary = Gate.Phase.onQExpr(
      for (c1 <- control1; c2 <- control2)
        yield 1.0 * c1.toInt * c2.toInt / (1 << c1.length.max(c2.length)))

    add(new OracleUnitary(unitary, true))
    add(new ClassicalPosition((cell map (_.toDouble)) + Vector2(1, 1), cells))
    add(new Collider(cells.pure[QExpr]))
    add(new Renderable(1, texture))
    add(new SpriteView(texture, scale = Vector2(2.0, 2.0).pure[QExpr]))
  }
}

/** Contains the animations for lasers. */
private object Rotator {
  /** The animation for an inactive laser. */
  private val offTexture: TextureRegion = new TextureRegion(new Texture(resolve("sprites/rotator.png")))

  /** The animation for an active laser. */
  private val onTexture: TextureRegion = new TextureRegion(new Texture(resolve("sprites/rotator.png")))
}
