package superposition.entity

import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP_PINGPONG
import com.badlogic.gdx.graphics.g2d.{Animation, TextureRegion}
import com.badlogic.gdx.utils.{Array => GArray}
import superposition.component.Animated.keepTime
import superposition.component.Player.isWalking
import superposition.component._
import superposition.entity.Cat.{deadAnimation, standingAnimation, walkingAnimation}
import superposition.game.ResourceResolver.resolve
import superposition.math.QExpr.QExpr
import superposition.math.Vector2

/** Schrödinger's cat.
  *
  * @param id the entity ID of Schrödinger's cat
  * @param multiverse the multiverse
  * @param initialCell the initial cell position
  */
final class Cat(id: Int, multiverse: Multiverse, initialCell: Vector2[Int]) extends Entity {
  locally {
    val alive = multiverse.allocate("Is Alive?", true, if (_) "Alive" else "Dead")
    val absolutePosition = multiverse.allocateMeta((initialCell map (_.toDouble)) + Vector2(0.5, 0.5))
    val cell = multiverse.allocate("Position", initialCell)

    val animation = alive.value map {
      if (_)
        if (isWalking) walkingAnimation
        else standingAnimation
      else deadAnimation
    }
    val animationTime = multiverse.allocateMeta(0f)
    val lastAnimation = multiverse.allocateMeta[Option[Animation[_]]](None)
    val frame = Animated.frame(animation, animationTime)

    add(new EntityId(id))
    add(new Player(alive))
    add(new QuantumPosition(absolutePosition, cell, Vector2(0.5, 0.5)))
    add(new PrimaryBit(Seq(alive)))
    add(
      new Renderable(
        2.pure[QExpr],
        for {
          isAlive <- alive.value
          currentCell <- cell.value
        } yield (isAlive, currentCell)
      )
    )
    add(new SpriteView(frame))
    add(new Animated(animation, animationTime, lastAnimation, keepTime))
  }
}

/** Contains the animations for Schrödinger's cat. */
private object Cat {

  /** The frames in the walking animation. */
  private val walkingFrames: Array[TextureRegion] =
    Animated.frames(new Texture(resolve("sprites/cat_anim.png")), 32, 32)

  /** The animation for a standing Schrödinger's cat. */
  private val standingAnimation: Animation[TextureRegion] = new Animation(0, GArray.`with`(walkingFrames(1)))

  /** The animation for a walking Schrödinger's cat. */
  private val walkingAnimation: Animation[TextureRegion] = new Animation(0.1f, new GArray(walkingFrames), LOOP_PINGPONG)

  /** The animation for a dead Schrödinger's cat. */
  private val deadAnimation: Animation[TextureRegion] =
    new Animation(0, new TextureRegion(new Texture(resolve("sprites/cat_dead.png"))))
}
