package superposition.game

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color.{BLACK, WHITE}
import com.badlogic.gdx.graphics.Texture
import superposition.game.Quball.QuballTexture
import superposition.game.ResourceResolver.resolve
import superposition.math.{Vector2d, Vector2i}
import superposition.quantum.{MetaId, StateId}

import scala.Function.const

/**
 * A quball is a quantum ball that can be either on or off.
 *
 * @param multiverse  the multiverse this quball belongs to
 * @param initialCell the initial cell of this quball
 */
private final class Quball(multiverse: MultiverseComponent, initialCell: Vector2i) extends Entity {
  val cell: StateId[Vector2i] = multiverse.allocate(initialCell)

  val position: MetaId[Vector2d] = multiverse.allocateMeta(initialCell.toVector2d + Vector2d(0.5, 0.5))

  val onOff: StateId[Boolean] = multiverse.allocate(false)

  val carried: StateId[Boolean] = multiverse.allocate(false)

  add(new SpriteComponent(
    texture = const(QuballTexture),
    position = _.meta(position),
    scale = const(Vector2d(1, 1)),
    color = universe => if (universe.state(onOff)) WHITE else BLACK,
    layer = 1))

   add(new UniverseComponent(primaryBit = Some(onOff), position = Some(cell)))
}

private object Quball {
  private val QuballTexture = new Texture(resolve("sprites/ball.png"))
}
