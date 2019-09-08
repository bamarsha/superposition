package superposition

import engine.core.Behavior.Entity
import engine.core.Game.dt
import engine.core.{Game, Input}
import engine.graphics.sprites.Sprite
import engine.util.Color.{BLACK, WHITE}
import engine.util.math.Vec2d
import extras.physics.PositionComponent
import org.lwjgl.glfw.GLFW._

/**
 * Contains settings and initialization for the player.
 */
private object Player {
  private val WalkGates: List[(Int, Gate.Value)] = List(
    (GLFW_KEY_W, Gate.Up),
    (GLFW_KEY_A, Gate.Left),
    (GLFW_KEY_S, Gate.Down),
    (GLFW_KEY_D, Gate.Right)
  )

  /**
   * Declares the player system.
   */
  def declareSystem(): Unit =
    Game.declareSystem(classOf[Player], (_: Player).step())
}

/**
 * The player character in the game.
 *
 * @param universe the universe this player belongs to
 * @param id       the universe object ID for this player
 * @param cell     the initial position for this player
 */
private final class Player(universe: Universe,
                           id: UniversalId,
                           cell: Cell) extends Entity with Copyable[Player] with Drawable {

  import Player._

  add(new PositionComponent(this, new Vec2d(cell.column + 0.5, cell.row + 0.5)))

  private val universeObject: UniverseObject = add(new UniverseObject(this, universe, id, cell))

  val sprite: DrawableSprite = add(new DrawableSprite(
    entity = this,
    sprite = Sprite.load(getClass.getResource("sprites/cat.png")),
    scale = new Vec2d(2, 2),
    color = WHITE
  ))

  private val bit: Bit = add(new Bit(this, true, on => sprite.color = if (on) WHITE else BLACK))

  private var carrying: Option[UniversalId] = None

  private var timeSinceLastWalk: Double = 0.15

  private def step(): Unit = {
    timeSinceLastWalk += dt
    if (timeSinceLastWalk >= 0.15) {
      val multiverse = universeObject.multiverse
      WalkGates.find { case (key, _) => Input.keyDown(key) }.map(_._2) match {
        case Some(gate) if multiverse.canApplyGate(gate, id, BitControl(id, on = true)) =>
          multiverse.applyGate(gate, id, BitControl(id, on = true))
          for (carry <- carrying) {
            multiverse.applyGate(gate, carry, BitControl(id, on = true), PositionControl(carry, universeObject.cell))
          }
          timeSinceLastWalk = 0
        case _ =>
      }
    }

    if (Input.keyJustPressed(GLFW_KEY_SPACE)) {
      toggleCarrying()
    }
  }

  private def toggleCarrying(): Unit =
    if (carrying.isEmpty)
      carrying = universeObject.universe.objects
        .find(o => o._2.entity != this && o._2.cell == universeObject.cell)
        .map(_._1)
    else
      carrying = None

  override def copy(): Player = {
    val player = new Player(universeObject.universe, universeObject.id, universeObject.cell)
    player.bit.on = bit.on
    player.carrying = carrying
    player
  }

  override def draw(): Unit = sprite.draw()
}
