package superposition.game

import com.badlogic.ashley.core.{ComponentMapper, Entity, Family}
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.math.Vector3
import superposition.game.LaserInput.{BeamMapper, QuantumMapper, hits, selected, target}
import superposition.math.Vector2i
import superposition.quantum.{StateId, Universe}

import scala.Function.const

private class LaserInput extends IteratingSystem(Family.all(classOf[Beam], classOf[Quantum]).get) {
  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    val multiverse = QuantumMapper.get(entity).multiverse
    val beam = BeamMapper.get(entity)
    if (input.isButtonJustPressed(0) && selected(multiverse, beam.source)) {
      multiverse.applyGate(beam.gate.multi controlled const(universe => hits(multiverse, universe, beam)), ())
      multiverse.updateMetaWith(beam.lastTarget)(const(universe => target(multiverse, universe, beam)))
      multiverse.updateMetaWith(beam.elapsedTime) { time => universe =>
        if (target(multiverse, universe, beam).isEmpty) time else 0
      }
    }
    multiverse.updateMetaWith(beam.elapsedTime)(time => const(time + deltaTime))
  }
}

private object LaserInput {
  private val BeamMapper: ComponentMapper[Beam] = ComponentMapper.getFor(classOf[Beam])

  private val QuantumMapper: ComponentMapper[Quantum] = ComponentMapper.getFor(classOf[Quantum])

  private def selected(multiverse: Multiverse, cell: Vector2i): Boolean = {
    val mouse = multiverse.camera.unproject(new Vector3(input.getX, input.getY, 0))
    cell == Vector2i(mouse.x.floor.toInt, mouse.y.floor.toInt)
  }

  private def target(multiverse: Multiverse, universe: Universe, beam: Beam): Option[Vector2i] =
    if (multiverse.allOn(universe, beam.controls))
      beam.path.take(Beam.Length) find { cell =>
        multiverse.isBlocked(universe, cell) || multiverse.allInCell(universe, cell).nonEmpty
      }
    else None

  private def hits(multiverse: Multiverse, universe: Universe, beam: Beam): Seq[StateId[Boolean]] =
    target(multiverse, universe, beam).iterator.to(Seq) flatMap (cell => multiverse.primaryBits(universe, cell))
}
