package superposition.system

import com.badlogic.ashley.core.{Engine, Entity, Family}
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Input.Keys.SPACE
import superposition.component._
import superposition.entity.Level
import superposition.math.{Gate, Translate, Vector2, X}
import superposition.system.PlayerInputSystem.{carryGate, updateCarriedPositions, updatePlayerPosition, walk}

import scala.Function.const
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
      multiverse.applyGate(carryGate(entity, carriables), ())
    }
    updatePlayerPosition(multiverse, entity, deltaTime)
    updateCarriedPositions(multiverse, entity, carriables, deltaTime)
  }
}

/** Player settings and functions for performing player actions. */
private object PlayerInputSystem {
  /** The speed of the player in cells per second. */
  private val Speed: Float = 3f

  /** A map from key code to unit vector representing the direction of movement. */
  private val WalkKeys: Map[Int, Vector2[Double]] = Map(
    Keys.W -> Vector2(0, 1),
    Keys.A -> Vector2(-1, 0),
    Keys.S -> Vector2(0, -1),
    Keys.D -> Vector2(1, 0))

  /** Returns a gate that walks the player and all carried entities to another cell.
    *
    * @param entity the player entity
    * @param carriables the carriable entities
    * @return the player walk gate
    */
  private def walkGate(entity: Entity, carriables: Iterable[Entity]): Gate[Vector2[Int]] = {
    val player = Player.mapper.get(entity)
    val position = QuantumPosition.mapper.get(entity)
    val walkPlayer: Gate[Vector2[Int]] = Translate.multi controlledMap { delta => universe =>
      if (universe.state(player.alive)) List((position.cell, delta))
      else Nil
    }
    val walkCarried: Gate[Vector2[Int]] = Translate.multi controlledMap { delta => universe =>
      if (universe.state(player.alive))
        carriables
          .filter(carriable => universe.state(Carriable.mapper.get(carriable).carried))
          .map(carriable => (QuantumPosition.mapper.get(carriable).cell, delta))
          .toList
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
  private def carryGate(entity: Entity, carriables: Iterable[Entity]): Gate[Unit] = {
    val player = Player.mapper.get(entity)
    val playerCell = QuantumPosition.mapper.get(entity).cell
    X.multi controlledMap const { universe =>
      carriables
        .filter { carriable =>
          val carriableCell = QuantumPosition.mapper.get(carriable).cell
          universe.state(player.alive) && universe.state(playerCell) == universe.state(carriableCell)
        }
        .map(Carriable.mapper.get(_).carried)
        .toList
    }
  }

  /** Returns the difference in player positions based on the player speed and the time elapsed since the last frame.
    *
    * @param deltaTime the time elapsed since the last frame
    * @return the difference in player positions
    */
  private def deltaPosition(deltaTime: Float): Vector2[Double] = {
    val delta = WalkKeys.foldLeft(Vector2(0d, 0d)) {
      case (delta, (key, direction)) =>
        if (input.isKeyPressed(key)) delta + direction
        else delta
    }
    if (delta.length == 0) delta else delta.withLength(Speed * deltaTime)
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
      if (dx != 0 && multiverse.applyGate(gate, Vector2(dx, 0))) dx else 0,
      if (dy != 0 && multiverse.applyGate(gate, Vector2(0, dy))) dy else 0)
    position.relative = (position.relative + delta).clamp(0, 1)
  }

  /** Updates the player absolute position metadata based on the cell and relative position.
    *
    * @param multiverse the multiverse
    * @param entity the player entity
    * @param deltaTime the time elapsed since the last frame
    */
  private def updatePlayerPosition(multiverse: Multiverse, entity: Entity, deltaTime: Float): Unit = {
    val player = Player.mapper.get(entity)
    val position = QuantumPosition.mapper.get(entity)
    multiverse.updateMetaWith(position.absolute) { pos => universe =>
      if (universe.state(player.alive)) {
        val targetPosition = (universe.state(position.cell) map (_.toDouble)) + position.relative
        pos.lerp(targetPosition, 1 - exp(-20 * deltaTime))
      } else pos
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
    val player = Player.mapper.get(entity)
    val playerPosition = QuantumPosition.mapper.get(entity)
    for (carriable <- carriables) {
      val carried = Carriable.mapper.get(carriable).carried
      val carriedPosition = QuantumPosition.mapper.get(carriable)
      multiverse.updateMetaWith(carriedPosition.absolute) { pos => universe =>
        val relativePos =
          if (universe.state(carried)) playerPosition.relative
          else Vector2(0.5, 0.5)
        val targetPos = (universe.state(carriedPosition.cell) map (_.toDouble)) + relativePos
        if (universe.state(player.alive))
          pos.lerp(targetPos, 1 - exp(-20 * deltaTime))
        else pos
      }
    }
  }
}
