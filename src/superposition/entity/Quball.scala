package superposition.entity

import cats.Apply
import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.functor.toFunctorOps
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color.WHITE
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import superposition.component._
import superposition.game.ResourceResolver.resolve
import superposition.math.QExpr.QExpr
import superposition.math.Vector2

/** A quball is a quantum ball that can be either on or off. */
object Quball {

  /** The texture for a quball. */
  private val texture: TextureRegion = new TextureRegion(new Texture(resolve("sprites/quball_1.png")))

  /** Creates a quball.
    *
    * @param id the entity ID of the quball
    * @param multiverse the multiverse
    * @param initialCell the initial cell of the quball
    */
  def apply(id: Int, multiverse: Multiverse, initialCell: Vector2[Int]): Entity = {
    val absolutePosition = multiverse.allocateMeta((initialCell map (_.toDouble)) + Vector2(0.5, 0.5))
    val onOff = multiverse.allocate("Is On?", false, if (_) "On" else "Off")
    val carried = multiverse.allocate("Is Carried?", false, if (_) "Carried" else "Dropped")
    val cell = multiverse.allocate("Position", initialCell)

    val entity = new Entity
    entity.add(new EntityId(id))
    entity.add(new QuantumPosition(absolutePosition, cell, Vector2(0.5, 0.5), true))
    entity.add(new PrimaryBit(Seq(onOff)))
    entity.add(new Activator(Seq(onOff)))
    entity.add(new Carriable(carried))
    entity.add(
      new Renderable(
        carried.value map (if (_) 3 else 1),
        Apply[QExpr].map3(onOff.value, carried.value, cell.value)((_, _, _))
      )
    )
    entity.add(
      new SpriteView(
        texture = texture.pure[QExpr],
        scale = carried.value map (if (_) Vector2(0.5, 0.5) else Vector2(0.75, 0.75)),
        color = WHITE.pure[QExpr]
      )
    )
    entity
  }
}
