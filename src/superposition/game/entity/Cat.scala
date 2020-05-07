package superposition.game.entity

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color.{BLACK, WHITE}
import com.badlogic.gdx.graphics.Texture
import superposition.game.ResourceResolver.resolve
import superposition.game.component._
import superposition.game.entity.Cat.CatTexture
import superposition.math.{Vector2d, Vector2i}

import scala.Function.const

final class Cat(multiverse: Multiverse, initialCell: Vector2i) extends Entity {
  locally {
    val alive = multiverse.allocate(true)
    val absolutePosition = multiverse.allocateMeta(initialCell.toVector2d + Vector2d(0.5, 0.5))
    val cell = multiverse.allocate(initialCell)

    add(new Toggle(alive))
    add(new Player(alive))
    add(new QuantumPosition(absolutePosition, cell, Vector2d(0.5, 0.5)))
    add(new SpriteView(
      texture = const(CatTexture),
      scale = const(Vector2d(2, 2)),
      color = universe => if (universe.state(alive)) WHITE else BLACK))
  }
}

private object Cat {
  private val CatTexture: Texture = new Texture(resolve("sprites/cat.png"))
}
