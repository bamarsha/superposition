package superposition.entity

import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import superposition.component._
import superposition.game.ResourceResolver.resolve
import superposition.math.QExpr.QExpr
import superposition.math.{StateId, Vector2}

/** A quball is a quantum ball that can be either on or off. */
object QuballMulti {

  /** The texture for a quball. */
  private val texture: TextureRegion = new TextureRegion(new Texture(resolve("sprites/quball_4.png")))

  /** The texture for a quball in Fourier space. */
  private val textureFourier: TextureRegion = new TextureRegion(new Texture(resolve("sprites/quball_4_fourier.png")))

  /** Creates a quball.
    *
    * @param id the entity ID of the quball
    * @param multiverse the multiverse
    * @param initialCell the initial cell of the quball
    */
  def apply(id: Int, multiverse: Multiverse, initialCell: Vector2[Int], size: Int): Entity = {
    val absolutePosition = multiverse.allocateMeta((initialCell map (_.toDouble)) + Vector2(0.5, 0.5))
    val bits = Seq.tabulate(size)(i => multiverse.allocate("Bit " + i, false, if (_) "1" else "0"))
    val carried = multiverse.allocate("Is Carried?", false, if (_) "Carried" else "Dropped")
    val cell = multiverse.allocate("Position", initialCell)
    val fourierBit = multiverse.allocate("In Fourier Space?", false, if (_) "Yes" else "No")

    val entity = new Entity
    entity.add(new EntityId(id))
    entity.add(new QuantumPosition(absolutePosition, cell, Vector2(0.5, 0.5), true))
    entity.add(new PrimaryBit(bits))
    entity.add(new Activator(bits))
    entity.add(new Carriable(carried))
    entity.add(new FourierBit(fourierBit))
    entity.add(
      new Renderable(
        1.pure[QExpr],
        for {
          bitValue <- QExpr.prepare((_: StateId[Boolean]).value)
          carriedValue <- carried.value
          cellValue <- cell.value
        } yield (bits map bitValue, carriedValue, cellValue)
      )
    )
    entity.add(
      new SpriteView(
        texture = fourierBit.value map (if (_) textureFourier else texture),
        scale = carried.value map (if (_) Vector2(0.5, 0.5) else Vector2(0.75, 0.75))
      )
    )
    entity
  }
}
