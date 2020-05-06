package superposition.game.system

import com.badlogic.ashley.core.{Engine, Entity, EntitySystem, Family}
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input.Keys.{N, R}
import superposition.game.LevelLoader
import superposition.game.component.{ClassicalPosition, Goal}
import superposition.game.system.LevelSystem.satisfied

import scala.jdk.CollectionConverters._

final class LevelSystem(levelLoader: LevelLoader) extends EntitySystem {
  private var goals: Iterable[Entity] = Nil

  override def addedToEngine(engine: Engine): Unit =
    goals = engine.getEntitiesFor(Family.all(classOf[Goal], classOf[ClassicalPosition]).get).asScala

  override def update(deltaTime: Float): Unit = {
    if (input.isKeyJustPressed(R)) {
      levelLoader.resetLevel()
    }
    if (input.isKeyJustPressed(N) || (goals forall satisfied)) {
      levelLoader.nextLevel()
    }
  }
}

private object LevelSystem {
  private def satisfied(entity: Entity): Boolean = {
    val goal = Goal.Mapper.get(entity)
    val cell = ClassicalPosition.Mapper.get(entity).cell
    goal.multiverse.universes forall (_.state(goal.needs) == cell)
  }
}
