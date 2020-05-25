package superposition.entity

import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.functor.toFunctorOps
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP
import com.badlogic.gdx.graphics.g2d.{Animation, TextureRegion}
import com.badlogic.gdx.utils.{Array => GArray}
import superposition.component.Animated.keepTime
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
  locally {
    val cells = direction match {
      case Direction.Up => Set(cell + Vector2(0, 1), cell, cell + Vector2(1, 0), cell + Vector2(1, 1))
      case Direction.Right => Set(cell + Vector2(1, 0), cell, cell + Vector2(0, 1), cell + Vector2(1, 1))
      case _ => Set(cell, cell + Vector2(1, 0), cell + Vector2(0, 1), cell + Vector2(1, 1))
    }

    val animation = control map (bits => if (bits.any) onAnimation else offAnimation)
    val animationTime = multiverse.allocateMeta(0f)
    val lastAnimation = multiverse.allocateMeta[Option[Animation[_]]](None)
    val frame = Animated.frame(animation, animationTime)

    add(new ClassicalPosition((cell map (_.toDouble)) + Vector2(1, 1), cells))
    add(new Collider(cells.pure[QExpr]))
    add(new Beam(multiverse, gate, direction, control))
    add(new Renderable(1, frame))
    add(new SpriteView(frame, Vector2(2.0, 2.0).pure[QExpr]))
    add(new Animated(animation, animationTime, lastAnimation, keepTime))
  }
}

/** Contains the animations for lasers. */
private object Laser {
  /** The animation for an inactive laser. */
  private val offAnimation: Animation[TextureRegion] =
    new Animation(0, new TextureRegion(new Texture(resolve("sprites/laser_off.png"))))

  /** The animation for an active laser. */
  private val onAnimation: Animation[TextureRegion] =
    new Animation(
      0.1f,
      new GArray(Animated.frames(new Texture(resolve("sprites/laser_anim.png")), 32, 32, 4)),
      LOOP)
}
