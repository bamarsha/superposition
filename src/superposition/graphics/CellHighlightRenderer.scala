package superposition.graphics

import cats.syntax.applicative.catsSyntaxApplicativeId
import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.GL20.GL_BLEND
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.utils.Disposable
import superposition.component.{CellHighlightView, QuantumPosition}
import superposition.entity.Level
import superposition.math.QExpr

/** Highlights all cells that are occupied by at least one entity in the multiverse.
  *
  * @param level a function that returns the current level
  */
final class CellHighlightRenderer(level: () => Option[Level]) extends Renderer with Disposable {
  /** A shape renderer. */
  private val shapeRenderer: ShapeRenderer = new ShapeRenderer

  override val family: Family = Family.all(CellHighlightView.getClass).get

  override def render(entity: Entity, deltaTime: Float): Unit = {
    val multiverse = level().get.multiverse
    val occupiedCells =
      (for {
        entity <- multiverse.entities if QuantumPosition.mapper.has(entity)
        position = QuantumPosition.mapper.get(entity)
        universe <- multiverse.universes
      } yield universe.state(position.cell)).toSet

    val multiverseView = level().get.multiverseView
    multiverseView.enqueueRenderer(().pure[QExpr]) { (_, _) =>
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

  override def dispose(): Unit = shapeRenderer.dispose()
}
