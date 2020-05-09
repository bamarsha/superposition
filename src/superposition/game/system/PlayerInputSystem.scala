package superposition.game.system

import com.badlogic.ashley.core.{Engine, Entity, Family}
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Input.Keys.SPACE
import superposition.game.component._
import superposition.game.entity.Level
import superposition.game.system.PlayerInputSystem.{carryGate, updateCarriedPositions, updatePlayerPosition, walk}
import superposition.math.{Vector2d, Vector2i}
import superposition.quantum.{Gate, Translate, X}

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
    carriables = engine.getEntitiesFor(Family.all(classOf[Carried], classOf[QuantumPosition]).get).asScala
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
  private val Speed: Float = 6.5f

  /** A map from key code to unit vector representing the direction of movement. */
  private val WalkKeys: Map[Int, Vector2d] = Map(
    Keys.W -> Vector2d(0, 1),
    Keys.A -> Vector2d(-1, 0),
    Keys.S -> Vector2d(0, -1),
    Keys.D -> Vector2d(1, 0))

  /** Returns a gate that walks the player and all carried entities to another cell.
    *
    * @param entity the player entity
    * @param carriables the carriable entities
    * @return the player walk gate
    */
  private def walkGate(entity: Entity, carriables: Iterable[Entity]): Gate[Vector2i] = {
    val player = Player.Mapper.get(entity)
    val position = QuantumPosition.Mapper.get(entity)
    val walkPlayer: Gate[Vector2i] = Translate.multi controlled { delta => universe =>
      if (universe.state(player.alive)) List((position.cell, delta))
      else Nil
    }
    val walkCarried: Gate[Vector2i] = Translate.multi controlled { delta => universe =>
      if (universe.state(player.alive))
        carriables
          .filter(carriable => universe.state(Carried.Mapper.get(carriable).carried))
          .map(carriable => (QuantumPosition.Mapper.get(carriable).cell, delta))
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
    val player = Player.Mapper.get(entity)
    val playerCell = QuantumPosition.Mapper.get(entity).cell
    X.multi controlled const { universe =>
      carriables
        .filter { carriable =>
          val carriableCell = QuantumPosition.Mapper.get(carriable).cell
          universe.state(player.alive) && universe.state(playerCell) == universe.state(carriableCell)
        }
        .map(Carried.Mapper.get(_).carried)
        .toList
    }
  }

  /** Returns the difference in player positions based on the player speed and the time elapsed since the last frame.
    *
    * @param deltaTime the time elapsed since the last frame
    * @return the difference in player positions
    */
  private def deltaPosition(deltaTime: Float): Vector2d = {
    val delta = WalkKeys.foldLeft(Vector2d(0, 0)) {
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
    val position = QuantumPosition.Mapper.get(entity)
    val rawDelta = deltaPosition(deltaTime)
    val dx = (position.relative.x + rawDelta.x).floor.toInt
    val dy = (position.relative.y + rawDelta.y).floor.toInt
    val gate = walkGate(entity, carriables)
    val delta = rawDelta - Vector2d(
      if (dx != 0 && multiverse.applyGate(gate, Vector2i(dx, 0))) dx else 0,
      if (dy != 0 && multiverse.applyGate(gate, Vector2i(0, dy))) dy else 0)
    position.relative = (position.relative + delta).clamp(0, 1)
  }

  /** Updates the player absolute position metadata based on the cell and relative position.
    *
    * @param multiverse the multiverse
    * @param entity the player entity
    * @param deltaTime the time elapsed since the last frame
    */
  private def updatePlayerPosition(multiverse: Multiverse, entity: Entity, deltaTime: Float): Unit = {
    val player = Player.Mapper.get(entity)
    val position = QuantumPosition.Mapper.get(entity)
    multiverse.updateMetaWith(position.absolute) { pos => universe =>
      if (universe.state(player.alive)) {
        val targetPosition = universe.state(position.cell).toVector2d + position.relative
        pos.lerp(targetPosition, 1 - exp(-10 * deltaTime))
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
    val player = Player.Mapper.get(entity)
    val playerPosition = QuantumPosition.Mapper.get(entity)
    for (carriable <- carriables) {
      val carried = Carried.Mapper.get(carriable).carried
      val carriedPosition = QuantumPosition.Mapper.get(carriable)
      multiverse.updateMetaWith(carriedPosition.absolute) { pos => universe =>
        val relativePos =
          if (universe.state(carried)) playerPosition.relative
          else Vector2d(0.5, 0.5)
        val targetPos = universe.state(carriedPosition.cell).toVector2d + relativePos
        if (universe.state(player.alive))
          pos.lerp(targetPos, 1 - exp(-10 * deltaTime))
        else pos
      }
    }
  }
}
