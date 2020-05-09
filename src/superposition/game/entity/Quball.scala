package superposition.game.entity

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color.{BLACK, WHITE}
import com.badlogic.gdx.graphics.Texture
import superposition.game.ResourceResolver.resolve
import superposition.game.component._
import superposition.game.entity.Quball.QuballTexture
import superposition.math.Vector2

import scala.Function.const

/** A quball is a quantum ball that can be either on or off.
  *
  * @param multiverse the multiverse
  * @param initialCell the initial cell of the quball
  */
final class Quball(multiverse: Multiverse, initialCell: Vector2[Int]) extends Entity {
  locally {
    val absolutePosition = multiverse.allocateMeta((initialCell map (_.toDouble)) + Vector2(0.5, 0.5))
    val onOff = multiverse.allocate(false)

    add(new QuantumPosition(absolutePosition, multiverse.allocate(initialCell), Vector2(0.5, 0.5)))
    add(new PrimaryBit(onOff))
    add(new Activator(onOff))
    add(new Carried(multiverse.allocate(false)))
    add(new SpriteView(
      texture = const(QuballTexture),
      scale = const(Vector2(1, 1)),
      color = universe => if (universe.state(onOff)) WHITE else BLACK,
      layer = 1))
  }
}

/** Contains the sprite texture for quballs. */
private object Quball {
  /** The sprite texture for a quball. */
  private val QuballTexture = new Texture(resolve("sprites/ball.png"))
}
