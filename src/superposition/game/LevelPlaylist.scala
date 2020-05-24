package superposition.game

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import superposition.component._
import superposition.entity._
import superposition.game.LevelLoader.loadLevel
import superposition.game.LevelPlaylist.{LevelFactory, addLevel, removeLevel}

/** A playlist of game levels.
  *
  * @param engine the entity engine
  */
final class LevelPlaylist(engine: Engine) {
  /** The list of level factories. */
  private var factories: Seq[LevelFactory] = Nil

  /** The current level. */
  private var _current: Option[Level] = None

  /** The current level. */
  def current: Option[Level] = _current

  /** Appends the sequence of tile maps to the playlist.
    *
    * @param loader the tile map loader
    * @param fileNames the file names for the tile maps
    */
  def appendAll(loader: TmxMapLoader, fileNames: Seq[String]): Unit =
    factories ++= fileNames map (fileName => () => loadLevel(loader.load("levels/" + fileName)))

  /** Advances to the next level in the playlist. */
  def next(): Unit = {
    factories = factories match {
      case Nil => Nil
      case _ :: next => next
    }
    play()
  }

  /** Plays the current level or resets the current level if it is already playing. */
  def play(): Unit = {
    current.foreach(removeLevel(engine))
    _current = factories.headOption map (_ ())
    current.foreach(addLevel(engine))
  }
}

/** Functions for creating and destroying levels. */
private object LevelPlaylist {
  /** A function that returns a new instance of a level. */
  private type LevelFactory = () => Level

  /** Adds a level and all of its entities to the engine.
    *
    * @param engine the entity engine
    * @param level the level to add
    */
  private def addLevel(engine: Engine)(level: Level): Unit = {
    engine.addEntity(level)
    level.entities.foreach(engine.addEntity)
    level.getComponent(classOf[Multiverse]).entities.foreach(engine.addEntity)
  }

  /** Removes a level and all of its entities from the engine and disposes its resources.
    *
    * @param engine the entity engine
    * @param level the level to remove
    */
  private def removeLevel(engine: Engine)(level: Level): Unit = {
    level.getComponent(classOf[Multiverse]).entities.foreach(engine.removeEntity)
    level.entities.foreach(engine.removeEntity)
    engine.removeEntity(level)
    level.dispose()
  }
}
