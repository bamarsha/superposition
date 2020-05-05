package superposition.game.system

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import superposition.game.component.{ClassicalPosition, QuantumObject, QuantumPosition, SpriteView}
import superposition.game.system.SpriteRenderSystem.{SpriteRenderFamily, compareLayers}

final class SpriteRenderSystem extends SortedIteratingSystem(SpriteRenderFamily, compareLayers) {
  private val batch: SpriteBatch = new SpriteBatch

  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    val multiverse = QuantumObject.Mapper.get(entity).multiverse
    val spriteView = SpriteView.Mapper.get(entity)
    batch.setProjectionMatrix(multiverse.camera.combined)
    for (universe <- multiverse.universes) {
      val position =
        if (ClassicalPosition.Mapper.has(entity))
          ClassicalPosition.Mapper.get(entity).absolute
        else universe.meta(QuantumPosition.Mapper.get(entity).absolute)
      multiverse.drawWithin(universe) {
        batch.begin()
        spriteView.draw(batch, universe, position)
        batch.end()
      }
    }
  }
}

private object SpriteRenderSystem {
  private val SpriteRenderFamily: Family =
    Family.all(classOf[SpriteView], classOf[QuantumObject])
      .one(classOf[ClassicalPosition], classOf[QuantumPosition])
      .get

  private def compareLayers(entity1: Entity, entity2: Entity): Int = {
    val layer1 = SpriteView.Mapper.get(entity1).layer
    val layer2 = SpriteView.Mapper.get(entity2).layer
    layer1.compare(layer2)
  }
}
