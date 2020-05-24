package superposition.graphics

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.gdx.graphics.g2d.{Batch, SpriteBatch}
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import superposition.component._
import superposition.entity.Level
import superposition.game.ResourceResolver.resolve
import superposition.graphics.ColorUtils.ShaderOps
import superposition.graphics.SpriteRenderer.absolutePosition
import superposition.math.{Universe, Vector2}

/** Renders sprites. */
final class SpriteRenderer(level: () => Option[Level]) extends Renderer with Disposable {
  /** The sprite shader program. */
  private val shader: ShaderProgram = new ShaderProgram(
    resolve("shaders/sprite.vert"),
    resolve("shaders/object.frag"))
  assert(shader.isCompiled, shader.getLog)

  /** The batch. */
  private val batch: Batch = new SpriteBatch(1000, shader)

  /** An array for holding color components. */
  private val colorArray: Array[Float] = Array.ofDim(4)

  override val family: Family = Family
    .all(classOf[SpriteView])
    .one(classOf[ClassicalPosition], classOf[QuantumPosition])
    .get

  override def render(entity: Entity, deltaTime: Float): Unit = {
    val multiverseView = level().get.multiverseView
    val dependentState = Renderable.mapper.get(entity).dependentState
    batch.setProjectionMatrix(multiverseView.camera.combined)
    multiverseView.enqueueRenderer(dependentState) { (universe, renderInfo) =>
      batch.begin()
      draw(entity, universe, renderInfo)
      batch.end()
    }
  }

  /** Draws the entity's sprite.
    *
    * @param entity the entity
    * @param universe the universe to draw in
    * @param renderInfo the rendering information for the universe
    */
  private def draw(entity: Entity, universe: Universe, renderInfo: UniverseRenderInfo): Unit = {
    val spriteView = SpriteView.mapper.get(entity)
    val scale = spriteView.scale(universe)
    val position = absolutePosition(entity, universe) - scale / 2

    shader.setUniformColor("color", spriteView.color(universe), colorArray)
    shader.setUniformColor("tintColor", renderInfo.color, colorArray)
    if (PrimaryBit.mapper.has(entity))
      for ((bit, i) <- PrimaryBit.mapper.get(entity).bits.zipWithIndex)
        shader.setUniformi("state[" + i + "]", if (universe.state(bit)) 1 else 0)
    if (LockCode.mapper.has(entity))
      for ((bit, i) <- LockCode.mapper.get(entity).bits.zipWithIndex)
        shader.setUniformi("state[" + i + "]", if (bit) 1 else 0)

    batch.setColor(spriteView.color(universe))
    batch.draw(spriteView.texture(universe), position.x.toFloat, position.y.toFloat, scale.x.toFloat, scale.y.toFloat)
    batch.flush()
  }

  override def dispose(): Unit = {
    shader.dispose()
    batch.dispose()
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
    if (ClassicalPosition.mapper.has(entity))
      ClassicalPosition.mapper.get(entity).absolute
    else universe.meta(QuantumPosition.mapper.get(entity).absolute)
}
