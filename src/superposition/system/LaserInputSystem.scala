package superposition.system

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx.input
import superposition.component.{Beam, ClassicalPosition, Multiverse}
import superposition.entity.Level
import superposition.math.{BitSeq, StateId, Universe, Vector2}
import superposition.system.LaserInputSystem.{beamHits, beamTarget}

import scala.Function.const

/** The system for activating lasers based on player input.
  *
  * @param level a function that returns the current level
  */
final class LaserInputSystem(level: () => Option[Level])
  extends IteratingSystem(Family.all(classOf[Beam], classOf[ClassicalPosition]).get) {
  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    val beam = Beam.mapper.get(entity)
    val cell = ClassicalPosition.mapper.get(entity).cells.head
    val multiverse = level().get.multiverse
    val multiverseView = level().get.multiverseView

    // Apply the gate when the laser is clicked.
    if (input.isButtonJustPressed(0) && multiverseView.isSelected(cell)) {
      multiverse.applyGate(beam.gate.multi controlledMap const(beamHits(multiverse, entity)), ())
      multiverse.updateMetaWith(beam.lastTarget)(const(beamTarget(multiverse, entity)))
      multiverse.updateMetaWith(beam.lastBeamSeq)(const(beam.control andThen Some[BitSeq]))
      multiverse.updateMetaWith(beam.elapsedTime) { time => universe =>
        if (beamTarget(multiverse, entity)(universe).isEmpty)
          time
        else 0
      }
    }
    multiverse.updateMetaWith(beam.elapsedTime)(time => const(time + deltaTime.toFloat))
  }
}

/** Laser settings and functions for computing properties of the laser beam. */
object LaserInputSystem {
  /** The maximum length of the laser beam. */
  private val beamLength: Int = 25

  /** Returns the cells in the path of the laser beam.
    *
    * @param entity the entity shooting the laser beam
    * @return the cells in the path of the laser beam
    */
  private def beamPath(entity: Entity): Seq[Vector2[Int]] = {
    val source = ClassicalPosition.mapper.get(entity).cells.head
    val direction = Beam.mapper.get(entity).direction
    LazyList.iterate(source)(_ + direction.toVector2).tail.take(beamLength)
  }

  /** The target of the laser beam.
    *
    * @param multiverse the multiverse
    * @param entity the entity shooting the laser beam
    * @param universe the universe
    * @return the target of the laser beam
    */
  private def beamTarget(multiverse: Multiverse, entity: Entity)(universe: Universe): Option[Vector2[Int]] = {
    if (Beam.mapper.get(entity).control(universe).any)
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
    beamTarget(multiverse, entity)(universe).iterator.to(Seq)
      .flatMap(multiverse.primaryBits(universe, _))
      .flatMap(Beam.mapper.get(entity).control(universe).filter(_))
}
