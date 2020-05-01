package superposition.game

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import superposition.game.Goal.GoalTexture
import superposition.game.ResourceResolver.resolve
import superposition.math.{Vector2d, Vector2i}
import superposition.quantum.StateId

import scala.Function.const

/**
 * A goal activates an action when the required object has reached the goal in every universe.
 *
 * @param multiverse the multiverse this goal belongs to
 * @param cell       the position of this goal
 * @param required   the position of the object that must reach this goal
 * @param action     the action to activate when the goal is reached
 */
private final class Goal(multiverse: Multiverse,
                         cell: Vector2i,
                         required: => StateId[Vector2i],
                         action: () => Unit)
    extends Entity {
  add(new SpriteView(
    texture = const(GoalTexture),
    position = const(cell.toVector2d + Vector2d(0.5, 0.5))))
}

//  private def step(): Unit =
//    if ((multiverse forall (_.state(required) == cell)) || keyJustPressed(GLFW_KEY_N)) {
//      action()
//    }

private object Goal {
  private val GoalTexture = new Texture(resolve("sprites/key.png"))

  //  def declareSystem(): Unit = Game.declareSystem(classOf[Goal], (_: Goal).step())
}
