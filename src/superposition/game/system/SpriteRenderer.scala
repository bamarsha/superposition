package superposition.game.system

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import superposition.game.component.{ClassicalPosition, Multiverse, QuantumPosition, SpriteView}
import superposition.game.entity.Level
import superposition.game.system.SpriteRenderer.{SpriteRenderFamily, compareLayers}

final class SpriteRenderer(level: () => Option[Level])
  extends SortedIteratingSystem(SpriteRenderFamily, compareLayers) {
  private val batch: SpriteBatch = new SpriteBatch

  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    val multiverse = Multiverse.Mapper.get(level().get)
    val spriteView = SpriteView.Mapper.get(entity)
    batch.setProjectionMatrix(multiverse.camera.combined)
    for (universe <- multiverse.universes) {
      val position =
        if (ClassicalPosition.Mapper.has(entity))
          ClassicalPosition.Mapper.get(entity).absolute
        else universe.meta(QuantumPosition.Mapper.get(entity).absolute)
      multiverse.drawWithin(universe) { () =>
        batch.begin()
        spriteView.draw(batch, universe, position)
        batch.end()
      }
    }
  }
}

private object SpriteRenderer {
  private val SpriteRenderFamily: Family =
    Family.all(classOf[SpriteView]).one(classOf[ClassicalPosition], classOf[QuantumPosition]).get

  private def compareLayers(entity1: Entity, entity2: Entity): Int = {
    val layer1 = SpriteView.Mapper.get(entity1).layer
    val layer2 = SpriteView.Mapper.get(entity2).layer
    layer1.compare(layer2)
  }
}
