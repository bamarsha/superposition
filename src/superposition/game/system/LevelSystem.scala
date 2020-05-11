package superposition.game.system

import com.badlogic.ashley.core.{Engine, Entity, EntitySystem, Family}
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input.Keys.{N, R}
import superposition.game.LevelPlaylist
import superposition.game.component._
import superposition.game.system.LevelSystem.{entityAtExit, product}

import scala.jdk.CollectionConverters._

/** The system for changing levels or resetting the current level.
  *
  * @param levels the level playlist
  */
final class LevelSystem(levels: LevelPlaylist) extends EntitySystem {
  /** The level exit entities. */
  private var exits: Iterable[Entity] = Nil

  /** The player entities. */
  private var players: Iterable[Entity] = Nil

  override def addedToEngine(engine: Engine): Unit = {
    exits = engine.getEntitiesFor(Family.all(classOf[Exit], classOf[ClassicalPosition]).get).asScala
    players = engine.getEntitiesFor(Family.all(classOf[Player], classOf[QuantumPosition]).get).asScala
  }

  override def update(deltaTime: Float): Unit = {
    if (input.isKeyJustPressed(R)) {
      levels.play()
    }
    val multiverse = levels.current.get.multiverse
    if (input.isKeyJustPressed(N) || (product(players, exits) exists entityAtExit(multiverse).tupled)) {
      levels.next()
    }
  }
}

/** Functions for computing properties about the state of the level. */
private object LevelSystem {
  /** Returns true if the entity with a quantum position has reached the exit in all universes.
    *
    * @param multiverse the multiverse
    * @param entity the entity with a quantum position
    * @param exit the exit entity
    * @return true if the entity has reached the exit in all universes
    */
  private def entityAtExit(multiverse: Multiverse)(entity: Entity, exit: Entity): Boolean = {
    val entityCell = QuantumPosition.Mapper.get(entity).cell
    val exitCells = ClassicalPosition.Mapper.get(exit).cells
    multiverse.universes forall (exitCells contains _.state(entityCell))
  }

  /** The Cartesian product of two iterables.
    *
    * @param xs the first iterable
    * @param ys the second iterable
    * @tparam A the type of the first iterable's elements
    * @tparam B the type of the second iterable's elements
    * @return the Cartesian product of `xs` and `ys`
    */
  private def product[A, B](xs: Iterable[A], ys: Iterable[B]): Iterable[(A, B)] = for {
    x <- xs
    y <- ys
  } yield (x, y)
}
