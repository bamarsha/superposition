package superposition.entity

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color.WHITE
import com.badlogic.gdx.graphics.Texture
import superposition.component._
import superposition.entity.Quball._
import superposition.game.ResourceResolver.resolve
import superposition.math.Vector2

import scala.Function.const

/** A quball is a quantum ball that can be either on or off.
  *
  * @param id the entity ID of the quball
  * @param multiverse the multiverse
  * @param initialCell the initial cell of the quball
  */
final class Quball(id: Int, multiverse: Multiverse, initialCell: Vector2[Int]) extends Entity {
  locally {
    val absolutePosition = multiverse.allocateMeta((initialCell map (_.toDouble)) + Vector2(0.5, 0.5))
    val onOff = multiverse.allocate("Is On?", false, if (_) "On" else "Off")
    val carried = multiverse.allocate("Is Carried?", false, if (_) "Carried" else "Dropped")
    val cell = multiverse.allocate("Position", initialCell)

    add(new EntityId(id))
    add(new QuantumPosition(absolutePosition, cell, Vector2(0.5, 0.5)))
    add(new PrimaryBit(onOff))
    add(new Activator(onOff))
    add(new Carriable(carried))
    add(new Renderable(1, universe => (universe.state(onOff), universe.state(carried), universe.state(cell))))
    add(new SpriteView(
      texture = universe => if (universe.state(onOff)) onTexture else offTexture,
      scale = universe => Vector2(1d, 1d) * (if (universe.state(carried)) .5 else .75),
      color = const(WHITE)))
  }
}

/** Contains the sprite texture for quballs. */
private object Quball {
  /** The sprite texture for a quball. */
  private val onTexture = new Texture(resolve("sprites/quball_v1_1.png"))
  private val offTexture = new Texture(resolve("sprites/quball_v1_0.png"))
}
