package superposition.entity

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color.WHITE
import com.badlogic.gdx.graphics.Texture
import superposition.component._
import superposition.entity.QuballMulti._
import superposition.game.ResourceResolver.resolve
import superposition.math.Vector2

import scala.Function.const


/** A quball is a quantum ball that can be either on or off.
  *
  * @param id the entity ID of the quball
  * @param multiverse the multiverse
  * @param initialCell the initial cell of the quball
  */
final class QuballMulti(id: Int, multiverse: Multiverse, initialCell: Vector2[Int], size: Int) extends Entity {
  locally {
    val absolutePosition = multiverse.allocateMeta((initialCell map (_.toDouble)) + Vector2(0.5, 0.5))
    val bits = Seq.tabulate(size)(i => multiverse.allocate("Bit " + i, false, if (_) "1" else "0"))
    val carried = multiverse.allocate("Is Carried?", false, if (_) "Carried" else "Dropped")
    val cell = multiverse.allocate("Position", initialCell)

    add(new EntityId(id))
    add(new QuantumPosition(absolutePosition, cell, Vector2(0.5, 0.5)))
    add(new PrimaryBit(bits))
    add(new Activator(bits))
    add(new Carriable(carried))
    add(new Renderable(1, universe => (bits.map(universe.state(_)), universe.state(carried), universe.state(cell))))
    add(new SpriteView(
      texture = universe => if (universe.state(bits.head)) onTexture else offTexture,
      scale = universe => Vector2(1d, 1d) * (if (universe.state(carried)) .5 else .75),
      color = const(WHITE)))
  }
}

/** Contains the sprite texture for quballs. */
private object QuballMulti {
  /** The sprite texture for a quball. */
  private val onTexture = new Texture(resolve("sprites/quball_v1_1111.png"))
  private val offTexture = new Texture(resolve("sprites/quball_v1_0000.png"))
}


