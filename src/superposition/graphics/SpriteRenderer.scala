package superposition.graphics

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import superposition.game.ResourceResolver.resolve
import superposition.game.component.{ClassicalPosition, QuantumPosition, Renderable, SpriteView}
import superposition.game.entity.Level
import superposition.graphics.Extensions._
import superposition.graphics.SpriteRenderer.absolutePosition
import superposition.math.Vector2
import superposition.quantum.Universe

/** Renders sprites. */
final class SpriteRenderer(level: () => Option[Level]) extends Renderer {

  private val shader: ShaderProgram = new ShaderProgram(
    resolve("shaders/sprite.vert"),
    resolve("shaders/spriteMixColor.frag"))

  assert(shader.isCompiled)

  // TODO: SpriteBatch is disposable.
  /** A sprite batch. */
  private val batch: SpriteBatch = new SpriteBatch(1000, shader)

  override val family: Family = Family
    .all(classOf[SpriteView])
    .one(classOf[ClassicalPosition], classOf[QuantumPosition])
    .get

  override def render(entity: Entity, deltaTime: Float): Unit = {
    val multiverseView = level().get.multiverseView
    batch.setProjectionMatrix(multiverseView.camera.combined)
    multiverseView.enqueueRenderer(Renderable.Mapper.get(entity).dependentState) { (universe, urp) =>
      batch.begin()
      draw(entity, universe, urp)
      batch.end()
    }
  }

  /** Draws the entity's sprite.
    *
    * @param entity the entity
    * @param universe the universe to draw in
    */
  private def draw(entity: Entity, universe: Universe, urp: Option[UniverseRenderParams]): Unit = {
    val spriteView = SpriteView.Mapper.get(entity)
    val scale = spriteView.scale(universe)
    val position = absolutePosition(entity, universe) - scale / 2
    shader.setUniformColor("color", urp.color)
    batch.setColor(spriteView.color(universe))
    batch.draw(spriteView.texture(universe), position.x.toFloat, position.y.toFloat, scale.x.toFloat, scale.y.toFloat)
    batch.flush()
  }
}

private object SpriteRenderer {

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
