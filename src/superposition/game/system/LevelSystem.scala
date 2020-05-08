package superposition.game.system

import com.badlogic.ashley.core.{Engine, Entity, EntitySystem, Family}
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input.Keys.{N, R}
import superposition.game.LevelPlaylist
import superposition.game.component.{ClassicalPosition, Goal, Multiverse}
import superposition.game.system.LevelSystem.satisfied

import scala.jdk.CollectionConverters._

/** The system for changing levels or resetting the current level.
  *
  * @param levels the level playlist
  */
final class LevelSystem(levels: LevelPlaylist) extends EntitySystem {
  /** The goal entities. */
  private var goals: Iterable[Entity] = Nil

  override def addedToEngine(engine: Engine): Unit =
    goals = engine.getEntitiesFor(Family.all(classOf[Goal], classOf[ClassicalPosition]).get).asScala

  override def update(deltaTime: Float): Unit = {
    if (input.isKeyJustPressed(R)) {
      levels.play()
    }
    val multiverse = Multiverse.Mapper.get(levels.current.get)
    if (input.isKeyJustPressed(N) || (goals forall satisfied(multiverse))) {
      levels.next()
    }
  }
}

/** Functions for computing properties about the state of the level. */
private object LevelSystem {
  /** Returns true if all of the goal's needs are satisfied.
    *
    * @param multiverse the multiverse
    * @param entity the goal entity
    * @return true if all of the goal's needs are satisfied
    */
  private def satisfied(multiverse: Multiverse)(entity: Entity): Boolean = {
    val goal = Goal.Mapper.get(entity)
    val cell = ClassicalPosition.Mapper.get(entity).cell
    multiverse.universes forall (_.state(goal.needs) == cell)
  }
}
