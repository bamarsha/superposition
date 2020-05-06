package superposition.game.system

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx.{gl, input}
import com.badlogic.gdx.graphics.Color.RED
import com.badlogic.gdx.graphics.GL20.GL_BLEND
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.{Filled, Line}
import superposition.game.component.{Beam, ClassicalPosition, QuantumObject}
import superposition.game.system.LaserSystem.{beamHits, beamTarget, drawBeam}
import superposition.math.Direction.{Down, Left, Right, Up}
import superposition.math.Vector2i
import superposition.quantum.{StateId, Universe}

import scala.Function.const
import scala.math.min

final class LaserSystem
  extends IteratingSystem(Family.all(classOf[Beam], classOf[QuantumObject], classOf[ClassicalPosition]).get) {
  private val shapeRenderer: ShapeRenderer = new ShapeRenderer

  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    val beam = Beam.Mapper.get(entity)
    val cell = ClassicalPosition.Mapper.get(entity).cell
    val multiverse = QuantumObject.Mapper.get(entity).multiverse

    if (input.isButtonJustPressed(0) && multiverse.isSelected(cell)) {
      multiverse.applyGate(beam.gate.multi controlled const(beamHits(entity)), ())
      multiverse.updateMetaWith(beam.lastTarget)(const(beamTarget(entity)))
      multiverse.updateMetaWith(beam.elapsedTime) { time => universe =>
        if (beamTarget(entity)(universe).isEmpty) time else 0
      }
    }
    multiverse.updateMetaWith(beam.elapsedTime)(time => const(time + deltaTime))

    for (universe <- multiverse.universes) {
      multiverse.drawWithin(universe) {
        drawBeam(shapeRenderer, universe, entity)
      }
    }
  }
}

private object LaserSystem {
  private val BeamLength: Int = 25

  private val BeamDuration: Double = 0.2

  private val FadeDuration: Double = 0.3

  private def beamPath(entity: Entity): Seq[Vector2i] = {
    val source = ClassicalPosition.Mapper.get(entity).cell
    val direction = Beam.Mapper.get(entity).direction
    LazyList.iterate(source)(_ + direction.toVector2i).tail.take(BeamLength)
  }

  private def beamTarget(entity: Entity)(universe: Universe): Option[Vector2i] = {
    val multiverse = QuantumObject.Mapper.get(entity).multiverse
    val controls = Beam.Mapper.get(entity).controls
    if (multiverse.allOn(universe, controls))
      beamPath(entity) find { cell =>
        multiverse.isBlocked(universe, cell) || multiverse.allInCell(universe, cell).nonEmpty
      }
    else None
  }

  private def beamHits(entity: Entity)(universe: Universe): Seq[StateId[Boolean]] = {
    val multiverse = QuantumObject.Mapper.get(entity).multiverse
    beamTarget(entity)(universe).iterator.to(Seq) flatMap (cell => multiverse.toggles(universe, cell))
  }

  private def drawBeam(shapeRenderer: ShapeRenderer, universe: Universe, entity: Entity): Unit = {
    val multiverse = QuantumObject.Mapper.get(entity).multiverse
    val source = ClassicalPosition.Mapper.get(entity).cell
    val beam = Beam.Mapper.get(entity)
    shapeRenderer.setProjectionMatrix(multiverse.camera.combined)

    if (multiverse.isSelected(source)) {
      shapeRenderer.begin(Line)
      shapeRenderer.setColor(RED)
      shapeRenderer.rect(source.x, source.y, 1, 1)
      shapeRenderer.end()
    }

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
