package superposition.component

import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps
import com.badlogic.ashley.core.{Component, ComponentMapper}
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.{Animation, TextureRegion}
import superposition.math.MetaId
import superposition.math.QExpr.QExpr

/** Adds an animation to an entity.
  *
  * @param animation the animation
  * @param time the time elapsed since the animation started
  * @param lastAnimation the animation used in the last rendering frame
  */
final class Animated(
    val animation: QExpr[Animation[_]],
    val time: MetaId[Float],
    val lastAnimation: MetaId[Option[Animation[_]]])
  extends Component

/** Contains the component mapper for animation components. */
object Animated {
  /** The component mapper for animation components. */
  val mapper: ComponentMapper[Animated] = ComponentMapper.getFor(classOf[Animated])

  /** Returns the texture region for the animation frame at the current time.
    *
    * @param animation the animation
    * @param time the time elapsed since the animation started
    * @return the texture region for the current frame
    */
  def frame(animation: QExpr[Animation[TextureRegion]], time: MetaId[Float]): QExpr[TextureRegion] =
    for {
      currentAnimation <- animation
      currentTime <- time.value
    } yield currentAnimation.getKeyFrame(currentTime)

  /** Splits a sprite sheet into a sequence of animation frames.
    *
    * @param texture the sprite sheet texture
    * @param frameWidth the width of each animation frame
    * @param frameHeight the height of each animation frame
    * @param frames the number of frames in the animation
    * @return the animation frame textures
    */
  def frames(texture: Texture, frameWidth: Int, frameHeight: Int, frames: Int): Array[TextureRegion] =
    TextureRegion.split(texture, frameWidth, frameHeight).flatten.take(frames)
}
