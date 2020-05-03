package superposition.game.system

import com.badlogic.ashley.core.{Engine, Entity, EntitySystem, Family}
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input.Keys.{N, R}
import superposition.game.LevelLoader
import superposition.game.component.Goal
import superposition.game.system.LevelController.satisfied

import scala.jdk.CollectionConverters._

final class LevelController(levelLoader: LevelLoader) extends EntitySystem {
  private var goals: Iterable[Entity] = Nil

  override def addedToEngine(engine: Engine): Unit =
    goals = engine.getEntitiesFor(Family.all(classOf[Goal]).get).asScala

  override def update(deltaTime: Float): Unit = {
    if (input.isKeyJustPressed(R)) {
      levelLoader.resetLevel()
    }
    if (input.isKeyJustPressed(N) || (goals map Goal.Mapper.get forall satisfied)) {
      levelLoader.nextLevel()
    }
  }
}

private object LevelController {
  private def satisfied(goal: Goal): Boolean =
    goal.multiverse.universes forall (_.state(goal.needs) == goal.cell)
}
