package superposition.entity

import cats.syntax.functor.toFunctorOps
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode.REVERSED
import com.badlogic.gdx.graphics.g2d.{Animation, TextureRegion}
import com.badlogic.gdx.utils.{Array => GArray}
import superposition.component.Animated.invertTime
import superposition.component._
import superposition.entity.Lock.{lockAnimation, unlockAnimation}
import superposition.game.ResourceResolver.resolve
import superposition.math.QExpr.QExpr
import superposition.math.{BitSeq, Vector2}

/** A door blocks movement unless its control expression is true.
  *
  * @param multiverse the multiverse the lock belongs to
  * @param cell the cell position of the lock
  * @param code the code for the lock
  * @param control the control for the lock
  */
final class Lock(id: Int, multiverse: Multiverse, cell: Vector2[Int], code: Seq[Boolean], control: QExpr[BitSeq])
  extends Entity {
  locally {
    val unlocking = unlockAnimation(code.length)
    val locking = lockAnimation(code.length)
    val animation = control map (bits => if (bits == BitSeq(code: _*)) unlocking else locking)
    val animationTime = multiverse.allocateMeta(0f)
    val lastAnimation = multiverse.allocateMeta[Option[Animation[_]]](None)
    val frame = Animated.frame(animation, animationTime)

    add(new EntityId(id))
    add(new ClassicalPosition((cell map (_.toDouble)) + Vector2(0.5, 0.5)))
    add(new LockCode(code))
    add(new Renderable(1, frame))
    add(new SpriteView(frame))
    add(new Animated(animation, animationTime, lastAnimation, invertTime))
  }
}

/** Contains the animations for locks. */
private object Lock {
  /** Returns the frames in the lock animation.
    *
    * @param codeLength the length of the lock code
    * @return the frames in the lock animation
    */
  private def frames(codeLength: Int): GArray[TextureRegion] =
    new GArray(Animated.frames(
      new Texture(resolve(s"sprites/lock_${codeLength}_anim.png")), 16, 16))

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
