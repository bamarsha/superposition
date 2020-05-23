package superposition.graphics

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Color.RED
import com.badlogic.gdx.graphics.GL20.GL_BLEND
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.{Filled, Line}
import com.badlogic.gdx.utils.Disposable
import superposition.component.{Beam, ClassicalPosition, Renderable}
import superposition.entity.Level
import superposition.graphics.BeamRenderer._
import superposition.graphics.ColorUtils.ColorOps
import superposition.math.Direction.{Down, Left, Right, Up}
import superposition.math.{Universe, Vector2}

import scala.math.min

/** Renders laser beams.
  *
  * @param level a function that returns the current level
  */
final class BeamRenderer(level: () => Option[Level]) extends Renderer with Disposable {
  /** The shape renderer. */
  private val shapeRenderer: ShapeRenderer = new ShapeRenderer

  override val family: Family = Family.all(classOf[Beam], classOf[ClassicalPosition]).get

  override def render(entity: Entity, deltaTime: Float): Unit = {
    val multiverseView = level().get.multiverseView
    shapeRenderer.setProjectionMatrix(multiverseView.camera.combined)
    val cell = ClassicalPosition.mapper.get(entity).cells.head
    val dependentState = Renderable.mapper.get(entity).dependentState
    multiverseView.enqueueRenderer(dependentState) { (universe, renderInfo) =>
      if (multiverseView.isSelected(cell)) {
        drawOutline(shapeRenderer, cell)
      }
      drawBeam(entity, universe, renderInfo)
    }
  }

  /** Draws the laser beam.
    *
    * @param entity the entity shooting the laser beam
    * @param universe the universe
    * @param renderInfo the rendering information for the universe
    */
  private def drawBeam(entity: Entity, universe: Universe, renderInfo: UniverseRenderInfo): Unit = {
    val source = ClassicalPosition.mapper.get(entity).cells.head
    val beam = Beam.mapper.get(entity)
    for (target <- universe.meta(beam.lastTarget);
         beamSeq <- universe.meta(beam.lastBeamSeq)
         if universe.meta(beam.elapsedTime) <= beamDuration + fadeDuration) {
      val opacity = min(fadeDuration, beamDuration + fadeDuration - universe.meta(beam.elapsedTime)) / fadeDuration
      gl.glEnable(GL_BLEND)
      shapeRenderer.begin(Filled)
      val numBeams = beamSeq.get.length
      val beamWidth = singleWidth / numBeams
      val beamOffset = (i: Int) => ((i + .5f) / numBeams - .5f) * totalWidth + .5f - beamWidth / 2
      for (i <- Seq.range(0, numBeams)) if (beamSeq.get(i)) {
        shapeRenderer.setColor(new Color(1, 1, 1, opacity).fromHsv(i * huePerBeam, 1, 1).mixed(renderInfo.color))
        beam.direction match {
          case Left | Right =>
            shapeRenderer.rect(source.x + 0.5f, source.y + beamOffset(i), target.x - source.x, beamWidth)
          case Up | Down =>
            shapeRenderer.rect(source.x + beamOffset(i), source.y + 0.5f, beamWidth, target.y - source.y)
        }
      }
      shapeRenderer.end()
      gl.glDisable(GL_BLEND)
    }
  }

  override def dispose(): Unit = shapeRenderer.dispose()
}

/** Functions for rendering laser beams. */
private object BeamRenderer {
  /** The amount of time that the laser beam shines at full intensity. */
  private val beamDuration: Float = 0.2f

  /** The amount of time that the laser beam takes to fade away. */
  private val fadeDuration: Float = 0.3f

  /** Hue change per beam */
  private val huePerBeam: Float = 360f / 6

  /** Total possible width of a multi-controlled beam */
  private val totalWidth: Float = .6f

  /** Total width of a singly-controlled beam */
  private val singleWidth: Float = .3f

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
}
