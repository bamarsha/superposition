package superposition.game.system

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.Color.RED
import com.badlogic.gdx.graphics.GL20.GL_BLEND
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.{Filled, Line}
import superposition.game.component.{Beam, ClassicalPosition}
import superposition.game.entity.Level
import superposition.game.system.RenderingSystem.RenderingAction
import superposition.math.Direction.{Down, Left, Right, Up}
import superposition.math.Vector2
import superposition.quantum.Universe

import scala.math.min

/** Renders laser beams. */
object BeamRenderer {
  /** The component family used by the beam renderer. */
  val BeamRendererFamily: Family = Family.all(classOf[Beam], classOf[ClassicalPosition]).get

  /** The amount of time that the laser beam shines at full intensity. */
  private val BeamDuration: Double = 0.2

  /** The amount of time that the laser beam takes to fade away. */
  private val FadeDuration: Double = 0.3

  /** Renders a beam for an entity.
    *
    * @param level a function that returns the current level
    * @return the rendering action
    */
  def renderBeam(level: () => Option[Level]): RenderingAction = {
    // TODO: ShapeRenderer is disposable.
    val shapeRenderer = new ShapeRenderer
    entity => {
      val multiverseView = level().get.multiverseView
      val cell = ClassicalPosition.Mapper.get(entity).cells.head
      shapeRenderer.setProjectionMatrix(multiverseView.camera.combined)
      multiverseView.enqueueDrawing { universe =>
        if (multiverseView.isSelected(cell)) {
          drawOutline(shapeRenderer, cell)
        }
        drawBeam(shapeRenderer, entity, universe)
      }
    }
  }

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
