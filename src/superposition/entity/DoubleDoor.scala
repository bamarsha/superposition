package superposition.entity

import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.functor.toFunctorOps
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode.REVERSED
import com.badlogic.gdx.graphics.g2d.{Animation, TextureRegion}
import com.badlogic.gdx.utils.{Array => GArray}
import superposition.component.Animated.invertTime
import superposition.component._
import superposition.game.ResourceResolver.resolve
import superposition.math.QExpr.QExpr
import superposition.math.Vector2

/** A door blocks movement unless its control expression is true. */
object DoubleDoor {

  /** The frames in the door animation. */
  private val frames: GArray[TextureRegion] =
    new GArray(Animated.frames(new Texture(resolve("sprites/door_anim.png")), 32, 16).take(14))

  /** The door opening animation. */
  private val openAnimation: Animation[TextureRegion] = new Animation(0.02f, frames)

  /** The door closing animation. */
  private val closeAnimation: Animation[TextureRegion] = new Animation(0.02f, frames, REVERSED)

  /** Creates a double door.
    *
    * @param multiverse the multiverse the door belongs to
    * @param cell the cell position of the door
    * @param control the control for the door
    */
  def apply(multiverse: Multiverse, cell: Vector2[Int], control: QExpr[Boolean]): Entity = {
    val animation = control map (if (_) openAnimation else closeAnimation)
    val animationTime = multiverse.allocateMeta(0f)
    val lastAnimation = multiverse.allocateMeta[Option[Animation[_]]](None)
    val frame = Animated.frame(animation, animationTime)

    val entity = new Entity
    entity.add(new ClassicalPosition((cell map (_.toDouble)) + Vector2(1.0, 0.5)))
    entity.add(new Collider(control map (if (_) Set.empty else Set(cell, cell + Vector2(1, 0)))))
    entity.add(new Renderable(1.pure[QExpr], frame))
    entity.add(new SpriteView(frame, scale = Vector2(2.0, 1.0).pure[QExpr]))
    entity.add(new Animated(animation, animationTime, lastAnimation, invertTime))
    entity
  }
}
