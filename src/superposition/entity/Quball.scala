package superposition.entity

import cats.Apply
import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.functor.toFunctorOps
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color.WHITE
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import superposition.component._
import superposition.entity.Quball._
import superposition.game.ResourceResolver.resolve
import superposition.math.QExpr.QExpr
import superposition.math.Vector2

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
    add(new QuantumPosition(absolutePosition, cell, Vector2(0.5, 0.5), true))
    add(new PrimaryBit(Seq(onOff)))
    add(new Activator(Seq(onOff)))
    add(new Carriable(carried))
    add(
      new Renderable(
        carried.value map (if (_) 3 else 1),
        Apply[QExpr].map3(onOff.value, carried.value, cell.value)((_, _, _))
      )
    )
    add(
      new SpriteView(
        texture = texture.pure[QExpr],
        scale = carried.value map (if (_) Vector2(0.5, 0.5) else Vector2(0.75, 0.75)),
        color = WHITE.pure[QExpr]
      )
    )
  }
}

/** Contains the texture for quballs. */
private object Quball {

  /** The texture for a quball. */
  private val texture: TextureRegion = new TextureRegion(new Texture(resolve("sprites/quball_1.png")))
}
