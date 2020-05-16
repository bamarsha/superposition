package superposition.entity

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import superposition.component.{Multiverse, MultiverseView, Renderable}

import scala.Function.const

/** A quantum level with a multiverse and tile map.
  *
  * @param multiverse the multiverse
  * @param multiverseView the multiverse view
  * @param entities level entities that are not part of the multiverse
  * @param mapShader the shader program used by the map renderer which should be disposed
  * @param mapBatch the batch used by the map renderer which should be disposed
  */
final class Level(
    val multiverse: Multiverse,
    val multiverseView: MultiverseView,
    val entities: Iterable[Entity],
    mapShader: ShaderProgram,
    mapBatch: Batch)
  extends Entity with Disposable {
  add(multiverse)
  add(multiverseView)
  add(new Renderable(0, const(())))

  override def dispose(): Unit = {
    mapShader.dispose()
    mapBatch.dispose()
  }
}
