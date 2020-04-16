package superposition.game

import engine.core.Behavior.Entity
import engine.core.{Game, Input}
import engine.graphics.sprites.Sprite
import org.lwjgl.glfw.GLFW
import superposition.math.Cell
import superposition.quantum.StateId

/**
 * Contains initialization for goals.
 */
private object Goal {
  /**
   * Declares the goal system.
   */
  def declareSystem(): Unit = Game.declareSystem(classOf[Goal], (_: Goal).step())

  private val GoalSprite = Sprite.load(getClass.getResource("sprites/key.png"))
}

/**
 * A goal activates a callback when the required object has reached the goal in every universe.
 *
 * @param multiverse the multiverse this goal belongs to
 * @param cell       the position of this goal
 * @param requires   the ID of the object that must reach this goal
 * @param callback   the callback to activate when the goal is reached
 */
private final class Goal(multiverse: Multiverse,
                         cell: Cell,
                         requires: => StateId[Cell],
                         private val callback: () => Unit) extends Entity {

  val sprite: SpriteComponent = add(new SpriteComponent(this, _ => Goal.GoalSprite, _ => cell.toVec2d.add(.5)))

  private def step(): Unit =
    if (multiverse.universes.forall(_.state(requires) == cell) || Input.keyJustPressed(GLFW.GLFW_KEY_N))
      callback()
}
