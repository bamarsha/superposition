package superposition.system

import com.badlogic.ashley.core.{Engine, Entity, EntitySystem, Family}
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input.Keys._
import superposition.component._
import superposition.game.LevelPlaylist

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
    exits = engine.getEntitiesFor(Family.all(Exit.getClass, classOf[ClassicalPosition]).get).asScala
    players = engine.getEntitiesFor(Family.all(classOf[Player], classOf[QuantumPosition]).get).asScala
  }

  override def update(deltaTime: Float): Unit = {
    if (input.isKeyJustPressed(P)) {
      levels.prev()
    } else if (input.isKeyJustPressed(R)) {
      levels.play()
    } else {
      val multiverse = levels.current.get.multiverse
      val allExitSquares = exits.flatMap(ClassicalPosition.mapper.get(_).cells).toSet
      val playersAtExits = players
        .map(QuantumPosition.mapper.get(_).cell)
        .forall(entityCell => multiverse.universes.map(_.state(entityCell)).forall(allExitSquares.contains))
      if (input.isKeyJustPressed(N) || playersAtExits) {
        levels.next()
      }
    }
  }
}
