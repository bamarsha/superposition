package superposition.entity

import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.functor.toFunctorOps
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import superposition.component._
import superposition.entity.DoubleDoor._
import superposition.game.ResourceResolver.resolve
import superposition.math.{QExpr, Vector2}

/** A door blocks movement unless its control expression is true.
  *
  * @param multiverse the multiverse the door belongs to
  * @param cell the cell position of the door
  * @param control the control for the door
  */
final class DoubleDoor(multiverse: Multiverse, cell: Vector2[Int], control: QExpr[Boolean]) extends Entity {
  add(new ClassicalPosition((cell map (_.toDouble)) + Vector2(1.0, 0.5)))
  add(new Collider(control map (if (_) Set.empty else Set(cell, cell + Vector2(1, 0)))))
  add(new Renderable(1, control))
  add(new SpriteView(closedTexture.pure[QExpr], control map (if (_) Vector2(0.0, 0.0) else Vector2(2.0, 1.0))))
}

/** Contains the sprite textures for doors. */
private object DoubleDoor {
  /** The sprite texture for a closed door. */
  private val closedTexture = new Texture(resolve("sprites/door.png"))
}



