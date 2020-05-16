package superposition.game.entity

import com.badlogic.ashley.core.Entity
import superposition.game.component.{Multiverse, MultiverseView, Renderable}

import scala.Function.const

/** A quantum level with a multiverse and tile map.
  *
  * @param multiverse the multiverse
  * @param multiverseView the multiverse view
  * @param entities level entities that are not part of the multiverse
  */
final class Level(
    val multiverse: Multiverse,
    val multiverseView: MultiverseView,
    val entities: Iterable[Entity])
  extends Entity {
  add(multiverse)
  add(multiverseView)
  add(new Renderable(0, const(())))
}
