package superposition.entity

import cats.syntax.applicative.catsSyntaxApplicativeId
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import superposition.component._
import superposition.entity.Oracle._
import superposition.game.ResourceResolver.resolve
import superposition.math.QExpr.QExpr
import superposition.math._

/** A oracle applies a quantum gate to the given qubit register.
  *
  * @param multiverse the multiverse
  * @param cell the position of the oracle
  * @param unitary the unitary that the oracle applies
  */
final class Oracle(
    multiverse: Multiverse,
    cell: Vector2[Int],
    unitary: Unitary,
    conjugate: Boolean)
  extends Entity {
  locally {
    add(new OracleUnitary(unitary, conjugate))
    add(new ClassicalPosition((cell map (_.toDouble)) + Vector2(.5, .5), Set(cell)))
    add(new Collider(Set(cell).pure[QExpr]))
    add(new Renderable(1, ().pure[QExpr]))
    add(new SpriteView(texture.pure[QExpr]))
  }
}

/** Contains the texture for oracles. */
private object Oracle {
  /** The texture for an oracle. */
  private val texture: TextureRegion = new TextureRegion(new Texture(resolve("sprites/fourier_active.png")))
}
