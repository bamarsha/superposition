package superposition.game

import com.badlogic.ashley.core._
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import superposition.game.MultiverseRenderer.{MultiverseMapper, SpriteViewMapper}

import scala.jdk.CollectionConverters._

private final class MultiverseRenderer extends EntitySystem {
  private var multiverses: Iterable[Entity] = Nil

  private var spriteViews: Iterable[Entity] = Nil

  private val spriteBatch: SpriteBatch = new SpriteBatch

  override def addedToEngine(engine: Engine): Unit = {
    multiverses = engine.getEntitiesFor(Family.all(classOf[Multiverse]).get).asScala
    spriteViews = engine.getEntitiesFor(Family.all(classOf[SpriteView]).get).asScala
  }

  override def update(deltaTime: Float): Unit =
    for (multiverse <- multiverses map MultiverseMapper.get) {
      spriteBatch.setProjectionMatrix(multiverse.camera.combined)
      spriteBatch.begin()
      for (universe <- multiverse.universes;
           spriteView <- spriteViews map SpriteViewMapper.get) {
        spriteView.draw(spriteBatch, universe)
      }
      spriteBatch.end()
    }
}

private object MultiverseRenderer {
  private val MultiverseMapper: ComponentMapper[Multiverse] = ComponentMapper.getFor(classOf[Multiverse])

  private val SpriteViewMapper: ComponentMapper[SpriteView] = ComponentMapper.getFor(classOf[SpriteView])
}
