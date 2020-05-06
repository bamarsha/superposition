package superposition.game.system

import com.badlogic.ashley.core.{Engine, Entity, EntitySystem, Family}
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input.Keys.{N, R}
import superposition.game.LevelPlaylist
import superposition.game.component.{ClassicalPosition, Goal, Multiverse}
import superposition.game.system.LevelSystem.satisfied

import scala.jdk.CollectionConverters._

final class LevelSystem(levels: LevelPlaylist) extends EntitySystem {
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

private object LevelSystem {
  private def satisfied(multiverse: Multiverse)(entity: Entity): Boolean = {
    val goal = Goal.Mapper.get(entity)
    val cell = ClassicalPosition.Mapper.get(entity).cell
    multiverse.universes forall (_.state(goal.needs) == cell)
  }
}
