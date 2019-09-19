package superposition

import engine.core.Behavior.Entity
import engine.graphics.sprites.Sprite
import engine.util.math.Vec2d
import extras.physics.PositionComponent

/**
 * Contains initialization for goals.
 */
private object Goal {
  /**
   * Declares the goal system.
   */
  def declareSystem(): Unit =
    Multiverse.declareSubsystem(classOf[Goal], step)

  private def step(multiverse: Multiverse, id: ObjectId, goals: Iterable[Goal]): Unit = {
    val allSatisfied = goals.forall(_.satisfied)
    if (allSatisfied && !goals.head.allSatisfied) {
      goals.head.callback()
    }
    goals.foreach(_.allSatisfied = allSatisfied)
  }
}

/**
 * A goal activates a callback when the required object has reached the goal in every universe.
 *
 * @param universe the universe this goal belongs to
 * @param id       the universe object ID for this goal
 * @param cell     the position of this goal
 * @param requires the ID of the object that must reach this goal
 * @param callback the callback to activate when the goal is reached
 */
private final class Goal(universe: Universe,
                         id: ObjectId,
                         cell: Cell,
                         requires: ObjectId,
                         private val callback: () => Unit) extends Entity with Copyable[Goal] with Drawable {
  add(new PositionComponent(this, new Vec2d(cell.column + 0.5, cell.row + 0.5)))

  private val obj: UniverseObject = add(new UniverseObject(this, universe, id, cell))

  private val sprite: SpriteComponent =
    add(new SpriteComponent(this, Sprite.load(getClass.getResource("sprites/key.png"))))

  private var allSatisfied: Boolean = false

  private def satisfied: Boolean =
    obj.universe.objects(requires).cell == cell

  override def copy(): Goal =
    new Goal(obj.universe, obj.id, obj.cell, requires, callback)

  override def draw(): Unit = sprite.draw()
}
