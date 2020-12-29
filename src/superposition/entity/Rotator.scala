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

/** A rotator applies a phase gate multi-controlled on the target qubits.
  *
  * @param multiverse the multiverse
  * @param cell the position of the rotator
  */
final class Rotator(multiverse: Multiverse, cell: Vector2[Int], control1: QExpr[BitSeq], control2: QExpr[BitSeq])
    extends Entity {
  locally {
    val cells = Set(cell, cell + Vector2(1, 0), cell + Vector2(0, 1), cell + Vector2(1, 1))
    val texture = onTexture.pure[QExpr]
    val phase =
      for (c1 <- control1; c2 <- control2)
        yield 1.0 * c1.toInt * c2.toInt / (1 << c1.length.max(c2.length))
    val unitary = Gate.Phase.onQExpr(phase)

    add(new OracleUnitary(unitary, true))
    add(new ClassicalPosition((cell map (_.toDouble)) + Vector2(1, 1), cells))
    add(new Collider(cells.pure[QExpr]))
    add(new Outline(phase map (_ != 0), cell map (_.toDouble), Vector2(2, 2)))
    add(new Renderable(1.pure[QExpr], texture))
    add(new SpriteView(texture, scale = Vector2(2.0, 2.0).pure[QExpr]))
  }
}

/** Contains the animations for rotators. */
private object Rotator {

  /** The animation for an active rotator. */
  private val onTexture: TextureRegion = new TextureRegion(new Texture(resolve("sprites/rotator.png")))
}
