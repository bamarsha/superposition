package superposition.system

import cats.Apply
import cats.syntax.functor.toFunctorOps
import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.ashley.systems.IteratingSystem
import superposition.component.Animated
import superposition.entity.Level
import superposition.math.QExpr.QExpr

import scala.Function.const

/** Updates animation timers.
  *
  * @param level a function that returns the current level
  */
final class AnimationSystem(level: () => Option[Level]) extends IteratingSystem(Family.all(classOf[Animated]).get) {
  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    val multiverse = level().get.multiverse
    val animated = Animated.mapper.get(entity)
    multiverse.updateMetaWith(animated.time) { time =>
      Apply[QExpr].map2(animated.animation, animated.lastAnimation.value) { (currentAnimation, lastAnimation) =>
        lastAnimation match {
          case Some(lastAnimation) if currentAnimation != lastAnimation =>
            animated.timeTransition(lastAnimation, currentAnimation, time + deltaTime)
          case _ => time + deltaTime
        }
      }
    }
    multiverse.updateMetaWith(animated.lastAnimation)(const(animated.animation map (Some(_))))
  }
}
