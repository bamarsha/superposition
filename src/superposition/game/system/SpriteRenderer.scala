package superposition.game.system

import com.badlogic.ashley.core.Family
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import superposition.game.component.{ClassicalPosition, QuantumPosition, SpriteView}
import superposition.game.entity.Level
import superposition.game.system.RenderingSystem.RenderingAction

/** Renders sprites. */
object SpriteRenderer {
  /** The component family used by the sprite renderer. */
  val SpriteRendererFamily: Family = Family
    .all(classOf[SpriteView])
    .one(classOf[ClassicalPosition], classOf[QuantumPosition])
    .get

  /** Renders a sprite for an entity.
    *
    * @param level a function that returns the current level
    * @return the rendering action
    */
  def renderSprite(level: () => Option[Level]): RenderingAction = {
    // TODO: SpriteBatch is disposable.
    val batch = new SpriteBatch
    entity => {
      val multiverseView = level().get.multiverseView
      val spriteView = SpriteView.Mapper.get(entity)
      batch.setProjectionMatrix(multiverseView.camera.combined)
      multiverseView.enqueueDrawing { universe =>
        val position =
          if (ClassicalPosition.Mapper.has(entity))
            ClassicalPosition.Mapper.get(entity).absolute
          else universe.meta(QuantumPosition.Mapper.get(entity).absolute)
        batch.begin()
        spriteView.draw(batch, universe, position)
        batch.end()
      }
    }
  }
}
