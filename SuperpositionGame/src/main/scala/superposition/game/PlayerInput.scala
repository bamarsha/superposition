package superposition.game

import com.badlogic.ashley.core.{ComponentMapper, Entity, Family}
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input.Keys
import superposition.game.PlayerInput._
import superposition.math.{Vector2d, Vector2i}
import superposition.quantum.{Gate, Translate}

import scala.math.exp

private class PlayerInput
  extends IteratingSystem(Family.all(classOf[Player], classOf[Position], classOf[Quantum]).get) {

  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    val multiverse = QuantumMapper.get(entity).multiverse
    val position = PositionMapper.get(entity)
    val player = PlayerMapper.get(entity)
    walk(multiverse, player, position, deltaTime)
    updatePosition(multiverse, player, position, deltaTime)
  }
}

private object PlayerInput {
  private val PlayerMapper: ComponentMapper[Player] = ComponentMapper.getFor(classOf[Player])

  private val PositionMapper: ComponentMapper[Position] = ComponentMapper.getFor(classOf[Position])

  private val QuantumMapper: ComponentMapper[Quantum] = ComponentMapper.getFor(classOf[Quantum])

  private val WalkKeys: Map[Int, Vector2d] = Map(
    Keys.W -> Vector2d(0, 1),
    Keys.A -> Vector2d(-1, 0),
    Keys.S -> Vector2d(0, -1),
    Keys.D -> Vector2d(1, 0))

  private def walkGate(player: Player, position: Position): Gate[Vector2i] =
    Translate.multi controlled { delta => universe =>
      if (universe.state(player.alive)) List((position.cell, delta)) else Nil
    }

  private def deltaPosition(deltaTime: Float, speed: Float): Vector2d = {
    val delta = WalkKeys.foldLeft(Vector2d(0, 0)) {
      case (delta, (key, direction)) =>
        if (input.isKeyPressed(key)) delta + direction
        else delta
    }
    if (delta.length == 0) delta else delta.withLength(speed * deltaTime)
  }

  private def snapPosition(delta: Double): Int =
    if (delta < -1e-3) -1
    else if (delta > 1 + 1e-3) 1
    else 0

  private def nextCell(start: Vector2d, delta: Vector2d): Vector2i = {
    val next = start + delta
    Vector2i(snapPosition(next.x), snapPosition(next.y))
  }

  private def walk(multiverse: Multiverse, player: Player, position: Position, deltaTime: Float): Unit = {
    def applyGate(delta: Vector2i) = multiverse.applyGate(walkGate(player, position), delta)

    val rawDelta = deltaPosition(deltaTime, player.speed)
    val Vector2i(dx, dy) = nextCell(position.relative, rawDelta)
    val effectiveDelta = rawDelta - Vector2d(
      if (dx != 0 && applyGate(Vector2i(dx, 0))) dx else 0,
      if (dy != 0 && applyGate(Vector2i(0, dy))) dy else 0)
    position.relative = (position.relative + effectiveDelta).clamp(0, 1)
  }

  private def updatePosition(multiverse: Multiverse, player: Player, position: Position, deltaTime: Float): Unit =
    multiverse.updateMetaWith(position.absolute) { pos => universe =>
      if (universe.state(player.alive)) {
        val targetPosition = universe.state(position.cell).toVector2d + position.relative
        pos.lerp(targetPosition, 1 - exp(-10 * deltaTime))
      } else pos
    }
}
