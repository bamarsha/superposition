package superposition.game.system

import com.badlogic.ashley.core._
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.GL20.GL_BLEND
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import superposition.game.component._

/** The multiverse renderer. */
final class MultiverseRenderer extends IteratingSystem(Family.all(classOf[Multiverse], classOf[MultiverseView]).get) {
  /** A shape renderer. */
  private val shapeRenderer: ShapeRenderer = new ShapeRenderer

  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    highlightOccupiedCells(entity)
    MultiverseView.Mapper.get(entity).update(deltaTime)
  }

  /** Highlights all cells occupied by an entity in the multiverse.
    *
    * @param entity the multiverse entity
    */
  private def highlightOccupiedCells(entity: Entity): Unit = {
    val multiverse = Multiverse.Mapper.get(entity)
    val occupiedCells =
      (for {
        entity <- multiverse.entities if QuantumPosition.Mapper.has(entity)
        position = QuantumPosition.Mapper.get(entity)
        universe <- multiverse.universes
      } yield universe.state(position.cell)).toSet

    gl.glEnable(GL_BLEND)
    shapeRenderer.setProjectionMatrix(MultiverseView.Mapper.get(entity).camera.combined)
    shapeRenderer.begin(ShapeType.Filled)
    shapeRenderer.setColor(1, 1, 1, 0.3f)
    for (cell <- occupiedCells) {
      shapeRenderer.rect(cell.x, cell.y, 1, 1)
    }
    shapeRenderer.end()
    gl.glDisable(GL_BLEND)
  }
}
