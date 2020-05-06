package superposition.game.entity

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color.{BLACK, WHITE}
import com.badlogic.gdx.graphics.Texture
import superposition.game.ResourceResolver.resolve
import superposition.game.component._
import superposition.game.entity.Quball.QuballTexture
import superposition.math.{Vector2d, Vector2i}

import scala.Function.const

/**
 * A quball is a quantum ball that can be either on or off.
 *
 * @param multiverse  the multiverse this quball belongs to
 * @param initialCell the initial cell of this quball
 */
final class Quball(multiverse: Multiverse, initialCell: Vector2i) extends Entity {
  locally {
    val position = multiverse.allocateMeta(initialCell.toVector2d + Vector2d(0.5, 0.5))
    val onOff = multiverse.allocate(false)

    add(new QuantumObject(multiverse))
    add(new QuantumPosition(position, multiverse.allocate(initialCell), Vector2d(0.5, 0.5)))
    add(new Toggle(onOff))
    add(new Activator(onOff))
    add(new Carried(multiverse.allocate(false)))
    add(new SpriteView(
      texture = const(QuballTexture),
      scale = const(Vector2d(1, 1)),
      color = universe => if (universe.state(onOff)) WHITE else BLACK,
      layer = 1))
  }
}

private object Quball {
  private val QuballTexture = new Texture(resolve("sprites/ball.png"))
}
