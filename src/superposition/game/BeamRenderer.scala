package superposition.game

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.Color.RED
import com.badlogic.gdx.graphics.GL20.GL_BLEND
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.{Filled, Line}
import superposition.game.BeamRenderer.{dependentState, drawBeam, drawOutline}
import superposition.game.component.{Beam, ClassicalPosition, Multiverse}
import superposition.game.entity.Level
import superposition.math.Direction.{Down, Left, Right, Up}
import superposition.math.Vector2
import superposition.quantum.Universe

import scala.math.min

/** Renders laser beams.
  *
  * @param level a function that returns the current level
  */
private final class BeamRenderer(level: () => Option[Level]) extends Renderer {
  // TODO: ShapeRenderer is disposable.
  /** A shape renderer. */
  private val shapeRenderer: ShapeRenderer = new ShapeRenderer

  override val family: Family = Family.all(classOf[Beam], classOf[ClassicalPosition]).get

  override def render(entity: Entity, deltaTime: Float): Unit = {
    val multiverseView = level().get.multiverseView
    shapeRenderer.setProjectionMatrix(multiverseView.camera.combined)
    val multiverse = level().get.multiverse
    val cell = ClassicalPosition.Mapper.get(entity).cells.head
    multiverseView.enqueueRenderer(dependentState(multiverse, entity)) { (universe, _) =>
      if (multiverseView.isSelected(cell)) {
        drawOutline(shapeRenderer, cell)
      }
      drawBeam(shapeRenderer, entity, universe)
    }
  }
}

/** Functions for rendering laser beams. */
private object BeamRenderer {
  /** The amount of time that the laser beam shines at full intensity. */
  private val BeamDuration: Double = 0.2

  /** The amount of time that the laser beam takes to fade away. */
  private val FadeDuration: Double = 0.3

  /** Returns the value of the quantum state that the sprite renderer depends on.
    *
    * @param multiverse the multiverse
    * @param entity the entity
    * @param universe the universe
    * @return the value of the dependent state
    */
  private def dependentState(multiverse: Multiverse, entity: Entity)(universe: Universe): Any =
    multiverse.allOn(universe, Beam.Mapper.get(entity).controls)

  /** Draws an outline around a cell.
    *
    * @param shapeRenderer a shape renderer
    * @param cell the cell
    */
  private def drawOutline(shapeRenderer: ShapeRenderer, cell: Vector2[Int]): Unit = {
    shapeRenderer.begin(Line)
    shapeRenderer.setColor(RED)
    shapeRenderer.rect(cell.x, cell.y, 1, 1)
    shapeRenderer.end()
  }

  /** Draws the laser beam.
    *
    * @param shapeRenderer a shape renderer
    * @param entity the entity shooting the laser beam
    * @param universe the universe
    */
  private def drawBeam(shapeRenderer: ShapeRenderer, entity: Entity, universe: Universe): Unit = {
    val source = ClassicalPosition.Mapper.get(entity).cells.head
    val beam = Beam.Mapper.get(entity)
    for (target <- universe.meta(beam.lastTarget)
         if universe.meta(beam.elapsedTime) <= BeamDuration + FadeDuration) {
      val opacity = min(FadeDuration, BeamDuration + FadeDuration - universe.meta(beam.elapsedTime)) / FadeDuration
      gl.glEnable(GL_BLEND)
      shapeRenderer.begin(Filled)
      shapeRenderer.setColor(1, 0, 0, opacity.toFloat)
      beam.direction match {
        case Left | Right => shapeRenderer.rect(source.x + 0.5f, source.y + 0.375f, target.x - source.x, 0.25f)
        case Up | Down => shapeRenderer.rect(source.x + 0.375f, source.y + 0.5f, 0.25f, target.y - source.y)
      }
      shapeRenderer.end()
      gl.glDisable(GL_BLEND)
    }
  }
}
