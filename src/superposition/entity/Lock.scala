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
import superposition.math.{BitSeq, Vector2}

/** A door blocks movement unless its control expression is true. */
object Lock {

  /** Creates a lock.
    *
    * @param multiverse the multiverse the lock belongs to
    * @param cell the cell position of the lock
    * @param code the code for the lock
    * @param control the control for the lock
    */
  def apply(id: Int, multiverse: Multiverse, cell: Vector2[Int], code: Seq[Boolean], control: QExpr[BitSeq]): Entity = {
    val isOpen = control map (_.withLength(code.length) == BitSeq(code: _*))
    val unlocking = unlockAnimation(code.length)
    val locking = lockAnimation(code.length)
    val animation = isOpen map (if (_) unlocking else locking)
    val animationTime = multiverse.allocateMeta(0f)
    val lastAnimation = multiverse.allocateMeta[Option[Animation[_]]](None)
    val frame = Animated.frame(animation, animationTime)

    val entity = new Entity
    entity.add(new EntityId(id))
    entity.add(new ClassicalPosition((cell map (_.toDouble)) + Vector2(0.5, 0.5)))
    entity.add(new LockCode(code, isOpen))
    entity.add(new Renderable(1.pure[QExpr], frame))
    entity.add(new SpriteView(frame))
    entity.add(new Animated(animation, animationTime, lastAnimation, invertTime))
    entity
  }

  /** Returns the frames in the lock animation.
    *
    * @param codeLength the length of the lock code
    * @return the frames in the lock animation
    */
  private def frames(codeLength: Int): GArray[TextureRegion] =
    new GArray(Animated.frames(new Texture(resolve(s"sprites/lock_${codeLength}_anim.png")), 16, 16))

  /** Returns the unlocking animation.
    *
    * @param codeLength the length of the lock code
    * @return the unlocking animation
    */
  private def unlockAnimation(codeLength: Int): Animation[TextureRegion] =
    new Animation(0.05f, frames(codeLength))

  /** Returns the locking animation.
    *
    * @param codeLength the length of the lock code
    * @return the locking animation
    */
  private def lockAnimation(codeLength: Int): Animation[TextureRegion] =
    new Animation(0.05f, frames(codeLength), REVERSED)
}
