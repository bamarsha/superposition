package superposition

import engine.core.Behavior.Entity
import engine.core.Game.dt
import engine.core.Input
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
    Multiverse.declareSubsystem(classOf[Player], step)

  private def step(multiverse: Multiverse, id: UniversalId, players: Iterable[Player]): Unit = {
    players.foreach(_.timeSinceLastWalk += dt)
    if (players.head.timeSinceLastWalk >= 0.15) {
      for (gate <- WalkGates.find { case (key, _) => Input.keyDown(key) }.map(_._2)) {
        walk(multiverse, id, gate, players)
      }
    }
    if (Input.keyJustPressed(GLFW_KEY_SPACE)) {
      players.foreach(_.toggleCarrying())
    }
  }

  private def walk(multiverse: Multiverse, id: UniversalId, gate: Gate.Value, players: Iterable[Player]): Unit = {
    val allCarried = players.map(p => (p.universeObject.cell, p.carrying)).collect {
      case (cell, Some(carrying)) => (cell, carrying)
    }.toSet
    val playersCanWalk = multiverse.canApplyGate(gate, id, BitControl(id, on = true))
    val carriedCanMove = allCarried.forall {
      case (cell, carrying) =>
        multiverse.canApplyGate(gate, carrying, BitControl(id, on = true), PositionControl(id, cell))
    }
    if (playersCanWalk && carriedCanMove) {
      for ((cell, carrying) <- allCarried) {
        multiverse.applyGate(gate, carrying, BitControl(id, on = true), PositionControl(id, cell))
      }
      multiverse.applyGate(gate, id, BitControl(id, on = true))
      players.foreach(_.timeSinceLastWalk = 0)
    }
  }
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

  private var timeSinceLastWalk = Double.PositiveInfinity

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
