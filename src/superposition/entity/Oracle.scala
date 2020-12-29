package superposition.entity

import cats.syntax.applicative.catsSyntaxApplicativeId
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import superposition.component._
import superposition.game.ResourceResolver.resolve
import superposition.math.QExpr.QExpr
import superposition.math._

/** A oracle applies a quantum gate to the given qubit register. */
object Oracle {

  /** The texture for an oracle. */
  private val texture: TextureRegion = new TextureRegion(new Texture(resolve("sprites/fourier_active.png")))

  /** Creates an oracle.
    *
    * @param multiverse the multiverse
    * @param cell the position of the oracle
    * @param unitary the unitary that the oracle applies
    */
  def apply(multiverse: Multiverse, cell: Vector2[Int], unitary: Unitary, conjugate: Boolean, name: String): Entity = {
    val entity = new Entity
    entity.add(new OracleUnitary(unitary, conjugate))
    entity.add(new ClassicalPosition((cell map (_.toDouble)) + Vector2(.5, .5), Set(cell)))
    entity.add(new Collider(Set(cell).pure[QExpr]))
    entity.add(new Outline(true.pure[QExpr], cell map (_.toDouble), Vector2(1, 1)))
    entity.add(new Renderable(1.pure[QExpr], ().pure[QExpr]))
    entity.add(new SpriteView(texture.pure[QExpr]))
    entity.add(new Text(name, (cell map (_.toDouble)) + Vector2(.5, 1)))
    entity
  }
}
