package superposition.system

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.ashley.systems.IteratingSystem
import superposition.component.{Animation, SpriteView}

/** Plays the animation for all entities. */
final class AnimationSystem extends IteratingSystem(Family.all(classOf[Animation], classOf[SpriteView]).get) {
  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    val animation = Animation.mapper.get(entity)
    val spriteView = SpriteView.mapper.get(entity)
    animation.time += deltaTime
    spriteView.texture = animation.animation.getKeyFrame(animation.time)
  }
}
