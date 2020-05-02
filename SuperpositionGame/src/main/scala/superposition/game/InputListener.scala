package superposition.game

import com.badlogic.ashley.core.{ComponentMapper, Entity, Family}
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input.Keys
import superposition.game.InputListener.{PlayerMapper, PositionMapper, QuantumMapper, deltaPosition}
import superposition.math.Vector2d

private class InputListener
    extends IteratingSystem(Family.all(classOf[Player], classOf[Position], classOf[Quantum]).get) {

  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    val multiverse = QuantumMapper.get(entity).multiverse
    val position = PositionMapper.get(entity)
    val speed = PlayerMapper.get(entity).speed
    position.relative += deltaPosition(deltaTime, speed)
    multiverse.updateMetaWith(position.absolute) { pos => universe =>
      val target = universe.state(position.cell).toVector2d + position.relative
      pos.lerp(target, 10 * deltaTime)
    }
  }
}

private object InputListener {
  private val PlayerMapper: ComponentMapper[Player] = ComponentMapper.getFor(classOf[Player])

  private val PositionMapper: ComponentMapper[Position] = ComponentMapper.getFor(classOf[Position])

  private val QuantumMapper: ComponentMapper[Quantum] = ComponentMapper.getFor(classOf[Quantum])

  private val WalkKeys: Map[Int, Vector2d] = Map(
    Keys.W -> Vector2d(0, 1),
    Keys.A -> Vector2d(-1, 0),
    Keys.S -> Vector2d(0, -1),
    Keys.D -> Vector2d(1, 0))

  private def deltaPosition(deltaTime: Float, speed: Float): Vector2d = {
    val delta = WalkKeys.foldLeft(Vector2d(0, 0)) {
      case (delta, (key, direction)) =>
        if (input.isKeyPressed(key)) delta + direction
        else delta
    }
    if (delta.length == 0) delta else delta.withLength(speed * deltaTime)
  }
}
