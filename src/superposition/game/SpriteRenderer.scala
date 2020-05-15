package superposition.game

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import superposition.game.component.{ClassicalPosition, QuantumPosition, SpriteView}
import superposition.game.entity.Level

/** Renders sprites. */
private final class SpriteRenderer(level: () => Option[Level]) extends Renderer {
  // TODO: SpriteBatch is disposable.
  /** A sprite batch. */
  private val batch: SpriteBatch = new SpriteBatch

  override val family: Family = Family
    .all(classOf[SpriteView])
    .one(classOf[ClassicalPosition], classOf[QuantumPosition])
    .get

  override def render(entity: Entity, deltaTime: Float): Unit = {
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
