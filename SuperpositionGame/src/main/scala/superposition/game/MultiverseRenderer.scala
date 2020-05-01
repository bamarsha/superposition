package superposition.game

import com.badlogic.ashley.core._
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import superposition.game.MultiverseRenderer.{MultiverseComponent, SpriteComponent}

import scala.jdk.CollectionConverters._

private final class MultiverseRenderer extends EntitySystem {
  private var multiverses: Iterable[Entity] = Nil

  private var spriteEntities: Iterable[Entity] = Nil

  private val spriteBatch: SpriteBatch = new SpriteBatch

  override def addedToEngine(engine: Engine): Unit = {
    multiverses = engine.getEntitiesFor(Family.all(classOf[MultiverseComponent]).get).asScala
    spriteEntities = engine.getEntitiesFor(Family.all(classOf[SpriteComponent]).get).asScala
  }

  override def update(deltaTime: Float): Unit =
    for (multiverse <- multiverses map MultiverseComponent.get) {
      spriteBatch.setProjectionMatrix(multiverse.camera.combined)
      spriteBatch.begin()
      for (universe <- multiverse.universes;
           sprite <- spriteEntities map SpriteComponent.get) {
        sprite.draw(spriteBatch, universe)
      }
      spriteBatch.end()
    }
}

private object MultiverseRenderer {
  private val MultiverseComponent: ComponentMapper[MultiverseComponent] =
    ComponentMapper.getFor(classOf[MultiverseComponent])

  private val SpriteComponent: ComponentMapper[SpriteComponent] = ComponentMapper.getFor(classOf[SpriteComponent])
}
