package superposition.game.system

import com.badlogic.ashley.core.{Entity, Family}
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx.input
import superposition.game.component.Beam.Length
import superposition.game.component.{Beam, ClassicalPosition, Multiverse}
import superposition.game.system.LaserSystem.{hits, target}
import superposition.math.Vector2i
import superposition.quantum.{StateId, Universe}

import scala.Function.const

final class LaserSystem extends IteratingSystem(Family.all(classOf[Beam], classOf[ClassicalPosition]).get) {
  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    val beam = Beam.Mapper.get(entity)
    val cell = ClassicalPosition.Mapper.get(entity).cell
    val multiverse = beam.multiverse

    if (input.isButtonJustPressed(0) && multiverse.isSelected(cell)) {
      multiverse.applyGate(beam.gate.multi controlled const(universe => hits(multiverse, universe, entity)), ())
      multiverse.updateMetaWith(beam.lastTarget)(const(universe => target(multiverse, universe, entity)))
      multiverse.updateMetaWith(beam.elapsedTime) { time => universe =>
        if (target(multiverse, universe, entity).isEmpty) time else 0
      }
    }
    multiverse.updateMetaWith(beam.elapsedTime)(time => const(time + deltaTime))

    for (universe <- multiverse.universes) {
      multiverse.drawWithin(universe) {
        beam.draw(universe, ClassicalPosition.Mapper.get(entity).cell)
      }
    }
  }
}

private object LaserSystem {
  private def path(entity: Entity): Seq[Vector2i] = {
    val source = ClassicalPosition.Mapper.get(entity).cell
    val direction = Beam.Mapper.get(entity).direction
    LazyList.iterate(source)(_ + direction.toVector2i).tail.take(Length)
  }

  private def target(multiverse: Multiverse, universe: Universe, entity: Entity): Option[Vector2i] = {
    val controls = Beam.Mapper.get(entity).controls
    if (multiverse.allOn(universe, controls))
      path(entity) find { cell =>
        multiverse.isBlocked(universe, cell) || multiverse.allInCell(universe, cell).nonEmpty
      }
    else None
  }

  private def hits(multiverse: Multiverse, universe: Universe, entity: Entity): Seq[StateId[Boolean]] =
    target(multiverse, universe, entity).iterator.to(Seq) flatMap (cell => multiverse.toggles(universe, cell))
}
