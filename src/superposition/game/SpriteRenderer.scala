package superposition.game

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import superposition.game.SpriteRenderer.draw
import superposition.game.component.{ClassicalPosition, QuantumPosition, Renderable, SpriteView}
import superposition.game.entity.Level
import superposition.math.Vector2
import superposition.quantum.Universe

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
    batch.setProjectionMatrix(multiverseView.camera.combined)
    multiverseView.enqueueRenderer(Renderable.Mapper.get(entity).dependentState) { (universe, _) =>
      batch.begin()
      draw(batch, entity, universe)
      batch.end()
    }
  }
}

private object SpriteRenderer {
  /** Draws the entity's sprite.
    *
    * @param batch the sprite batch to draw in
    * @param entity the entity
    * @param universe the universe to draw in
    */
  private def draw(batch: SpriteBatch, entity: Entity, universe: Universe): Unit = {
    val spriteView = SpriteView.Mapper.get(entity)
    val scale = spriteView.scale(universe)
    val position = absolutePosition(entity, universe) - scale / 2
    batch.setColor(spriteView.color(universe))
    batch.draw(spriteView.texture(universe), position.x.toFloat, position.y.toFloat, scale.x.toFloat, scale.y.toFloat)
  }

  /** Returns the absolute position of an entity that has either a classical or quantum position component.
    *
    * @param entity the entity
    * @param universe the universe
    * @return the absolute position of the entity
    */
  private def absolutePosition(entity: Entity, universe: Universe): Vector2[Double] =
    if (ClassicalPosition.Mapper.has(entity))
      ClassicalPosition.Mapper.get(entity).absolute
    else universe.meta(QuantumPosition.Mapper.get(entity).absolute)
}
