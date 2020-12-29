package superposition.entity

import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.functor.toFunctorOps
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import superposition.component._
import superposition.game.ResourceResolver.resolve
import superposition.math.QExpr.QExpr
import superposition.math.Vector2

/** A door blocks movement unless its control expression is true. */
object Door {

  /** The texture for a closed door. */
  private val closedTexture: TextureRegion = new TextureRegion(new Texture(resolve("sprites/door_closed.png")))

  /** The texture for an open door. */
  private val openTexture: TextureRegion = new TextureRegion(new Texture(resolve("sprites/door_open.png")))

  /** Creates a door.
    *
    * @param multiverse the multiverse the door belongs to
    * @param cell the cell position of the door
    * @param control the control for the door
    */
  def apply(multiverse: Multiverse, cell: Vector2[Int], control: QExpr[Boolean]): Entity = {
    val entity = new Entity
    entity.add(new ClassicalPosition((cell map (_.toDouble)) + Vector2(0.5, 0.5)))
    entity.add(new Collider(control map (if (_) Set.empty else Set(cell))))
    entity.add(new Renderable(1.pure[QExpr], control))
    entity.add(new SpriteView(control map (if (_) openTexture else closedTexture)))
    entity
  }
}
