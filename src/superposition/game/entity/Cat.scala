package superposition.game.entity

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color.{BLACK, WHITE}
import com.badlogic.gdx.graphics.Texture
import scalaz.syntax.functor._
import superposition.game.ResourceResolver.resolve
import superposition.game.component._
import superposition.game.entity.Cat.CatTexture
import superposition.math.Vector2

import scala.Function.const

/** Schrödinger's cat.
  *
  * @param multiverse the multiverse
  * @param initialCell the initial cell position
  */
final class Cat(multiverse: Multiverse, initialCell: Vector2[Int]) extends Entity {
  locally {
    val alive = multiverse.allocate(true)
    val absolutePosition = multiverse.allocateMeta((initialCell map (_.toDouble)) + Vector2(0.5, 0.5))
    val cell = multiverse.allocate(initialCell)

    add(new Toggle(alive))
    add(new Player(alive))
    add(new QuantumPosition(absolutePosition, cell, Vector2(0.5, 0.5)))
    add(new SpriteView(
      texture = const(CatTexture),
      scale = const(Vector2(2, 2)),
      color = universe => if (universe.state(alive)) WHITE else BLACK))
  }
}

/** Contains the sprite texture for Schrödinger's cat. */
private object Cat {
  /** The sprite texture for Schrödinger's cat. */
  private val CatTexture: Texture = new Texture(resolve("sprites/cat.png"))
}
