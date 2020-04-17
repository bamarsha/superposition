package superposition.game

import engine.core.Behavior.Entity
import engine.core.Game
import engine.core.Input.keyJustPressed
import engine.graphics.sprites.Sprite
import org.lwjgl.glfw.GLFW.GLFW_KEY_N
import superposition.game.Goal.GoalSprite
import superposition.math.Vec2i
import superposition.quantum.StateId

import scala.Function.const

private object Goal {
  def declareSystem(): Unit = Game.declareSystem(classOf[Goal], (_: Goal).step())

  private val GoalSprite = Sprite.load(getClass.getResource("sprites/key.png"))
}

/**
 * A goal activates an action when the required object has reached the goal in every universe.
 *
 * @param multiverse the multiverse this goal belongs to
 * @param cell       the position of this goal
 * @param required   the position of the object that must reach this goal
 * @param action     the action to activate when the goal is reached
 */
private final class Goal(multiverse: Multiverse,
                         cell: Vec2i,
                         required: => StateId[Vec2i],
                         action: () => Unit) extends Entity {
  add(new SpriteComponent(this, sprite = const(GoalSprite), position = const(cell.toVec2d add 0.5)))

  private def step(): Unit =
    if ((multiverse.universes forall (_.state(required) == cell)) || keyJustPressed(GLFW_KEY_N)) {
      action()
    }
}
