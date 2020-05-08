package superposition.game.system

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx.{gl, input}
import com.badlogic.gdx.graphics.Color.RED
import com.badlogic.gdx.graphics.GL20.GL_BLEND
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.{Filled, Line}
import superposition.game.component.{Beam, ClassicalPosition, Multiverse, MultiverseView}
import superposition.game.entity.Level
import superposition.game.system.LaserSystem.{beamHits, beamTarget, drawBeam, drawOutline}
import superposition.math.Direction.{Down, Left, Right, Up}
import superposition.math.Vector2i
import superposition.quantum.{StateId, Universe}

import scala.Function.const
import scala.math.min

/** The system for activating and drawing lasers.
  *
  * @param level a function that returns the current level
  */
final class LaserSystem(level: () => Option[Level])
  extends IteratingSystem(Family.all(classOf[Beam], classOf[ClassicalPosition]).get) {
  /** A shape renderer. */
  private val shapeRenderer: ShapeRenderer = new ShapeRenderer

  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    val beam = Beam.Mapper.get(entity)
    val cell = ClassicalPosition.Mapper.get(entity).cell
    val multiverse = level().get.multiverse
    val multiverseView = level().get.multiverseView

    // Apply the gate when the laser is clicked.
    if (input.isButtonJustPressed(0) && multiverseView.isSelected(cell)) {
      multiverse.applyGate(beam.gate.multi controlled const(beamHits(multiverse, entity)), ())
      multiverse.updateMetaWith(beam.lastTarget)(const(beamTarget(multiverse, entity)))
      multiverse.updateMetaWith(beam.elapsedTime) { time => universe =>
        if (beamTarget(multiverse, entity)(universe).isEmpty)
          time
        else 0
      }
    }
    multiverse.updateMetaWith(beam.elapsedTime)(time => const(time + deltaTime))

    // Draw the laser.
    shapeRenderer.setProjectionMatrix(multiverseView.camera.combined)
    multiverseView.draw { universe =>
      if (multiverseView.isSelected(cell)) {
        drawOutline(multiverseView, shapeRenderer, entity)
      }
      drawBeam(shapeRenderer, entity, universe)
    }
  }
}

/** Laser settings and functions for computing properties of lasers. */
private object LaserSystem {
  /** The maximum length of the laser beam. */
  private val BeamLength: Int = 25

  /** The amount of time that the laser beam shines at full intensity. */
  private val BeamDuration: Double = 0.2

  /** The amount of time that the laser beam takes to fade away. */
  private val FadeDuration: Double = 0.3

  /** Returns the cells in the path of the laser beam.
    *
    * @param entity the entity shooting the laser beam
    * @return the cells in the path of the laser beam
    */
  private def beamPath(entity: Entity): Seq[Vector2i] = {
    val source = ClassicalPosition.Mapper.get(entity).cell
    val direction = Beam.Mapper.get(entity).direction
    LazyList.iterate(source)(_ + direction.toVector2i).tail.take(BeamLength)
  }

  /** The target of the laser beam.
    *
    * @param multiverse the multiverse
    * @param entity the entity shooting the laser beam
    * @param universe the universe
    * @return the target of the laser beam
    */
  private def beamTarget(multiverse: Multiverse, entity: Entity)(universe: Universe): Option[Vector2i] = {
    val controls = Beam.Mapper.get(entity).controls
    if (multiverse.allOn(universe, controls))
      beamPath(entity) find { cell =>
        multiverse.isBlocked(universe, cell) || multiverse.allInCell(universe, cell).nonEmpty
      }
    else None
  }

  /** Returns the qubits that are hit by the laser beam.
    *
    * @param multiverse the multiverse
    * @param entity the entity shooting the laser beam
    * @param universe the universe
    * @return the qubits that are hit by the laser beam
    */
  private def beamHits(multiverse: Multiverse, entity: Entity)(universe: Universe): Seq[StateId[Boolean]] =
    (beamTarget(multiverse, entity)(universe).iterator.to(Seq)
      flatMap (cell => multiverse.toggles(universe, cell)))

  /** Draws an outline around the laser.
    *
    * @param multiverseView the multiverse view
    * @param shapeRenderer a shape renderer
    * @param entity the laser
    */
  private def drawOutline(multiverseView: MultiverseView, shapeRenderer: ShapeRenderer, entity: Entity): Unit = {
    val cell = ClassicalPosition.Mapper.get(entity).cell
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
    val source = ClassicalPosition.Mapper.get(entity).cell
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
