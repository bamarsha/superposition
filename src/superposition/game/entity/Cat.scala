package superposition.game.entity

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import superposition.game.ResourceResolver.resolve
import superposition.game.component._
import superposition.game.entity.Cat._
import superposition.math.Vector2

/** Schrödinger's cat.
  *
  * @param multiverse the multiverse
  * @param initialCell the initial cell position
  */
final class Cat(multiverse: Multiverse, initialCell: Vector2[Int]) extends Entity {
  locally {
    val alive = multiverse.allocate("Is Alive?", true, (a: Boolean) => if (a) "Alive" else "Dead")
    val absolutePosition = multiverse.allocateMeta((initialCell map (_.toDouble)) + Vector2(0.5, 0.5))
    val cell = multiverse.allocate("Position", initialCell)

    add(new PrimaryBit(alive))
    add(new Player(alive))
    add(new QuantumPosition(absolutePosition, cell, Vector2(0.5, 0.5)))
    add(new SpriteView(texture = universe => if (universe.state(alive)) CatTextureAlive else CatTextureDead))
  }
}

/** Contains the sprite texture for Schrödinger's cat. */
private object Cat {
  /** The sprite texture for Schrödinger's cat. */
  private val CatTextureAlive: Texture = new Texture(resolve("sprites/cat_alive.png"))
  private val CatTextureDead: Texture = new Texture(resolve("sprites/cat_dead.png"))
}
