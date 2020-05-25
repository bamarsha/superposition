package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input.Keys.{A, D, S, W}
import superposition.math.{StateId, Vector2}

/** The player component allows an entity to be controlled by the player.
  *
  * @param alive a qubit representing whether the player is alive
  */
final class Player(val alive: StateId[Boolean]) extends Component

/** Contains the component mapper for the player component. */
object Player {
  /** The component mapper for the player component. */
  val mapper: ComponentMapper[Player] = ComponentMapper.getFor(classOf[Player])

  /** A map from key code to the unit vector in the direction that the player should move when the key is pressed. */
  val walkingKeys: Map[Int, Vector2[Double]] = Map(
    W -> Vector2(0, 1),
    A -> Vector2(-1, 0),
    S -> Vector2(0, -1),
    D -> Vector2(1, 0))

  /** True if the player is currently pressing a walking key. */
  def isWalking: Boolean = walkingKeys.keys exists input.isKeyPressed
}
