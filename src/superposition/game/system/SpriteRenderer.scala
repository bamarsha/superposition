package superposition.game.system

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import superposition.game.component.{ClassicalPosition, QuantumPosition, SpriteView}
import superposition.game.entity.Level
import superposition.game.system.SpriteRenderer.{SpriteRenderFamily, compareLayers}

/** The sprite renderer.
  *
  * @param level a function that returns the current level
  */
final class SpriteRenderer(level: () => Option[Level])
  extends SortedIteratingSystem(SpriteRenderFamily, compareLayers) {
  /** A sprite batch. */
  private val batch: SpriteBatch = new SpriteBatch

  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    val multiverseView = level().get.multiverseView
    val spriteView = SpriteView.Mapper.get(entity)
    batch.setProjectionMatrix(multiverseView.camera.combined)
    multiverseView.draw { universe =>
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

/** Settings and functions for the sprite renderer. */
private object SpriteRenderer {
  /** The component family that should be rendered by the sprite renderer. */
  private val SpriteRenderFamily: Family =
    Family.all(classOf[SpriteView]).one(classOf[ClassicalPosition], classOf[QuantumPosition]).get

  /** Compares the layers of both entities and returns an integer whose sign indicates the result.
    *
    * @param entity1 the first entity
    * @param entity2 the second entity
    * @return the comparison result
    */
  private def compareLayers(entity1: Entity, entity2: Entity): Int = {
    val layer1 = SpriteView.Mapper.get(entity1).layer
    val layer2 = SpriteView.Mapper.get(entity2).layer
    layer1.compare(layer2)
  }
}
