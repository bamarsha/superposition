package superposition.graphics

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.GL20.GL_BLEND
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import superposition.game.component.{CellHighlighter, QuantumPosition}
import superposition.game.entity.Level

import scala.Function.const

/** Highlights all cells occupied by an entity in the multiverse.
  *
  * @param level a function that returns the current level
  */
final class CellHighlightRenderer(level: () => Option[Level]) extends Renderer {
  /** A shape renderer. */
  private val shapeRenderer: ShapeRenderer = new ShapeRenderer

  override def family: Family = Family.all(classOf[CellHighlighter]).get

  override def render(entity: Entity, deltaTime: Float): Unit = {
    val multiverse = level().get.multiverse
    val occupiedCells =
      (for {
        entity <- multiverse.entities if QuantumPosition.Mapper.has(entity)
        position = QuantumPosition.Mapper.get(entity)
        universe <- multiverse.universes
      } yield universe.state(position.cell)).toSet

    val multiverseView = level().get.multiverseView
    multiverseView.enqueueRenderer(const(())) { (_, _) =>
      gl.glEnable(GL_BLEND)
      shapeRenderer.setProjectionMatrix(multiverseView.camera.combined)
      shapeRenderer.begin(ShapeType.Filled)
      shapeRenderer.setColor(1, 1, 1, 0.3f)
      for (cell <- occupiedCells) {
        shapeRenderer.rect(cell.x, cell.y, 1, 1)
      }
      shapeRenderer.end()
      gl.glDisable(GL_BLEND)
    }
  }
}
