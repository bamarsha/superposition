package superposition.game.system

import com.badlogic.ashley.core._
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.GL20.GL_BLEND
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import superposition.game.component._

final class MultiverseRenderer extends IteratingSystem(Family.all(classOf[Multiverse]).get) {
  private val shapeRenderer: ShapeRenderer = new ShapeRenderer

  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    val multiverse = Multiverse.Mapper.get(entity)
    highlightOccupiedCells(multiverse)
    multiverse.updateShaderSettings(deltaTime)
  }

  private def highlightOccupiedCells(multiverse: Multiverse): Unit = {
    val occupiedCells =
      (for {
        entity <- multiverse.entities if QuantumPosition.Mapper.has(entity)
        position = QuantumPosition.Mapper.get(entity)
        universe <- multiverse.universes
      } yield universe.state(position.cell)).toSet

    gl.glEnable(GL_BLEND)
    shapeRenderer.setProjectionMatrix(multiverse.camera.combined)
    shapeRenderer.begin(ShapeType.Filled)
    shapeRenderer.setColor(1, 1, 1, 0.3f)
    for (cell <- occupiedCells) {
      shapeRenderer.rect(cell.x, cell.y, 1, 1)
    }
    shapeRenderer.end()
    gl.glDisable(GL_BLEND)
  }
}
