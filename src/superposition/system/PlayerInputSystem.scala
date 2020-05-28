package superposition.system

import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps
import com.badlogic.ashley.core.{Engine, Entity, Family}
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input.Keys.SPACE
import spire.implicits._
import superposition.component.Player.walkingKeys
import superposition.component._
import superposition.entity.Level
import superposition.math.Gate._
import superposition.math.QExpr.QExpr
import superposition.math._
import superposition.system.PlayerInputSystem.{carryGate, updateCarriedPositions, updatePlayerPosition, walk}

import scala.jdk.CollectionConverters._
import scala.math.exp

/** The system for taking player actions based on input.
  *
  * @param level a function that returns the current level
  */
final class PlayerInputSystem(level: () => Option[Level])
  extends IteratingSystem(Family.all(classOf[Player], classOf[QuantumPosition]).get) {
  /** The entities that can be carried by the player. */
  private var carriables: Iterable[Entity] = Nil

  override def addedToEngine(engine: Engine): Unit = {
    super.addedToEngine(engine)
    carriables = engine.getEntitiesFor(Family.all(classOf[Carriable], classOf[QuantumPosition]).get).asScala
  }

  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    val multiverse = level().get.multiverse
    walk(multiverse, entity, carriables, deltaTime)
    if (input.isKeyJustPressed(SPACE)) {
      multiverse.applyUnitary(carryGate(entity, carriables))
    }
    updatePlayerPosition(multiverse, entity, deltaTime)
    updateCarriedPositions(multiverse, entity, carriables, deltaTime)
  }
}

/** Player settings and functions for performing player actions. */
private object PlayerInputSystem {
  /** The speed of the player in cells per second. */
  private val speed: Float = 3f

  /** Returns a gate that walks the player and all carried entities to another cell.
    *
    * @param entity the player entity
    * @param carriables the carriable entities
    * @return the player walk gate
    */
  private def walkGate(entity: Entity, carriables: Iterable[Entity]): Gate[Vector2[Int]] = {
    val player = Player.mapper.get(entity)
    val position = QuantumPosition.mapper.get(entity)
    val walkPlayer: Gate[Vector2[Int]] = Translate.multi.controlledMap(
      player.alive.value map (isAlive => delta => if (isAlive) List((position.cell, delta)) else Nil))
    val walkCarried: Gate[Vector2[Int]] = Translate.multi.controlledMap {
      for {
        isAlive <- player.alive.value
        isCarried <- QExpr.prepare(Carriable.mapper.get(_: Entity).carried.value)
      } yield (delta: Vector2[Int]) =>
        if (isAlive)
          carriables
            .filter(isCarried)
            .map(carriable => (QuantumPosition.mapper.get(carriable).cell, delta))
            .toSeq
        else Nil
    }
    walkPlayer andThen walkCarried
  }

  /** Returns a gate that toggles the carried state of all carriable entities in the same cell as a live player.
    *
    * @param entity the player entity
    * @param carriables the carriable entities
    * @return the toggle carry gate
    */
  private def carryGate(entity: Entity, carriables: Iterable[Entity]): Unitary =
    X.multi.quantum {
      for {
        isAlive <- Player.mapper.get(entity).alive.value
        playerCell <- QuantumPosition.mapper.get(entity).cell.value
        carriableCell <- QExpr.prepare(QuantumPosition.mapper.get(_: Entity).cell.value)
      } yield {
        carriables
          .filter(carriable => isAlive && playerCell == carriableCell(carriable))
          .map(Carriable.mapper.get(_).carried)
          .toSeq
      }
    }

  /** Returns the difference in player positions based on the player speed and the time elapsed since the last frame.
    *
    * @param deltaTime the time elapsed since the last frame
    * @return the difference in player positions
    */
  private def deltaPosition(deltaTime: Float): Vector2[Double] = {
    val delta = walkingKeys.foldLeft(Vector2(0.0, 0.0)) {
      case (delta, (key, direction)) =>
        if (input.isKeyPressed(key)) delta + direction
        else delta
    }
    if (delta.length == 0) delta else delta.withLength(speed * deltaTime)
  }

  /** Walks the player and all carried entities based on input.
    *
    * @param multiverse the multiverse
    * @param entity the player entity
    * @param carriables the carriable entities
    * @param deltaTime the time elapsed since the last frame
    */
  private def walk(multiverse: Multiverse, entity: Entity, carriables: Iterable[Entity], deltaTime: Float): Unit = {
    val position = QuantumPosition.mapper.get(entity)
    val rawDelta = deltaPosition(deltaTime)
    val Vector2(dx, dy) = (position.relative + rawDelta) map (_.floor.toInt)
    val gate = walkGate(entity, carriables)
    val delta = rawDelta - Vector2(
      if (dx != 0 && multiverse.applyUnitary(gate(Vector2(dx, 0)))) dx else 0,
      if (dy != 0 && multiverse.applyUnitary(gate(Vector2(0, dy)))) dy else 0)
    position.relative = (position.relative + delta).clamp(0, 1)
  }

  /** Updates the player absolute position metadata based on the cell and relative position.
    *
    * @param multiverse the multiverse
    * @param entity the player entity
    * @param deltaTime the time elapsed since the last frame
    */
  private def updatePlayerPosition(multiverse: Multiverse, entity: Entity, deltaTime: Float): Unit = {
    val position = QuantumPosition.mapper.get(entity)
    multiverse.updateMetaWith(position.absolute) { absolutePos =>
      for {
        isAlive <- Player.mapper.get(entity).alive.value
        cell <- position.cell.value
        targetPos = (cell map (_.toDouble)) + position.relative
      } yield
        if (isAlive) absolutePos.lerp(targetPos, 1 - exp(-20 * deltaTime))
        else absolutePos
    }
  }

  /** Updates the absolute position metadata of carried entities based on the cell and relative position of the player
    * carrying them.
    *
    * @param multiverse the multiverse
    * @param entity the player entity
    * @param carriables the carriable entities
    * @param deltaTime the time elapsed since the last frame
    */
  private def updateCarriedPositions(multiverse: Multiverse,
                                     entity: Entity,
                                     carriables: Iterable[Entity],
                                     deltaTime: Float): Unit = {
    val playerPosition = QuantumPosition.mapper.get(entity)
    for (carriable <- carriables) {
      multiverse.updateMetaWith(QuantumPosition.mapper.get(carriable).absolute) { carriedPos =>
        for {
          isCarried <- Carriable.mapper.get(carriable).carried.value
          carriedCell <- QuantumPosition.mapper.get(carriable).cell.value
          isAlive <- Player.mapper.get(entity).alive.value
          relativePos = if (isCarried) playerPosition.relative else Vector2(0.5, 0.5)
          targetPos = (carriedCell map (_.toDouble)) + relativePos
        } yield
          if (isAlive) carriedPos.lerp(targetPos, 1 - exp(-20 * deltaTime))
          else carriedPos
      }
    }
  }
}
