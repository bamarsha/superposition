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
  private val WalkKeys: Map[Int, Vec2d] = Map(
    GLFW_KEY_W -> new Vec2d(0, 1),
    GLFW_KEY_A -> new Vec2d(-1, 0),
    GLFW_KEY_S -> new Vec2d(0, -1),
    GLFW_KEY_D -> new Vec2d(1, 0)
  )

  private var cellPosition = new Vec2d(0.5, 0.5)

  /**
   * Declares the player system.
   */
  def declareSystem(): Unit =
    Multiverse.declareSubsystem(classOf[Player], step)

  private def step(multiverse: Multiverse, id: UniversalId, players: Iterable[Player]): Unit = {
    cellPosition = walkGates(cellPositionFromInput).foldLeft(cellPositionFromInput)(
      (position, gate) => cellPositionFromGate(position, gate, walk(multiverse, id, gate, players))
    )
    updateAbsolutePositions(players)
    if (Input.keyJustPressed(GLFW_KEY_SPACE)) {
      toggleCarrying(multiverse, id, players)
    }
  }

  private def cellPositionFromInput: Vec2d = {
    val delta = WalkKeys.foldLeft(new Vec2d(0, 0)) {
      case (delta, (key, direction)) => if (Input.keyDown(key)) delta add direction else delta
    }
    cellPosition add (if (delta.length == 0) new Vec2d(0, 0) else delta.setLength(6.5 * dt))
  }

  private def cellPositionFromGate(cellPosition: Vec2d, gate: Gate.Value, success: Boolean): Vec2d = {
    require(Gate.positionGate(gate), "Gate is not a position gate")
    gate match {
      case Gate.Left => if (success) cellPosition add new Vec2d(1, 0) else cellPosition.setX(0)
      case Gate.Right => if (success) cellPosition add new Vec2d(-1, 0) else cellPosition.setX(1)
      case Gate.Down => if (success) cellPosition add new Vec2d(0, 1) else cellPosition.setY(0)
      case Gate.Up => if (success) cellPosition add new Vec2d(0, -1) else cellPosition.setY(1)
    }
  }

  private def walkGates(cellPosition: Vec2d): Iterable[Gate.Value] = {
    val x =
      if (cellPosition.x < -1e-3)
        Some(Gate.Left)
      else if (cellPosition.x > 1 + 1e-3)
        Some(Gate.Right)
      else
        None
    val y =
      if (cellPosition.y < -1e-3)
        Some(Gate.Down)
      else if (cellPosition.y > 1 + 1e-3)
        Some(Gate.Up)
      else
        None
    x ++ y
  }

  private def updateAbsolutePositions(players: Iterable[Player]): Unit = {
    val livePlayers = players.filter(_.bits.state("alive"))
    for (player <- livePlayers) {
      val position = player.position
      val cell = player.universeObject.cell
      position.value = position.value.lerp(cell.toVec2d add cellPosition, 10 * dt)
    }
    for (bits <- livePlayers.flatMap(_.universeObject.universe.bits.values)
         if bits.state.contains("carried")) {
      val position = bits.universeObject.position
      val target = if (bits.state("carried")) cellPosition else new Vec2d(0.5, 0.5)
      val cell = bits.universeObject.cell
      position.value = position.value.lerp(cell.toVec2d add target, 10 * dt)
    }
  }

  private def walk(multiverse: Multiverse, id: UniversalId, gate: Gate.Value, players: Iterable[Player]): Boolean = {
    require(Gate.positionGate(gate), "Gate is not a position gate")

    val allOtherBits = players.flatMap(_.universeObject.universe.bits.keySet).filter(_ != id).toSet
    val playersCanWalk = multiverse.canApplyGate(gate, id, BitControl(id, "alive" -> true))
    val carriedCanMove = allOtherBits.forall(otherId => multiverse.canApplyGate(
      gate, otherId,
      BitControl(id, "alive" -> true),
      BitControl(otherId, "carried" -> true)
    ))

    if (playersCanWalk && carriedCanMove) {
      for (otherId <- allOtherBits) {
        multiverse.applyGate(
          gate, otherId, None,
          BitControl(id, "alive" -> true),
          BitControl(otherId, "carried" -> true)
        )
      }
      multiverse.applyGate(gate, id, None, BitControl(id, "alive" -> true))
      true
    } else {
      false
    }
  }

  private def toggleCarrying(multiverse: Multiverse, id: UniversalId, players: Iterable[Player]): Unit = {
    val carryableIds = players.flatMap(player => {
      val universe = player.universeObject.universe
      universe
        .bitsInCell(player.universeObject.cell)
        .filter(otherId => otherId != id && universe.bits(otherId).state.contains("carried"))
    }).toSet
    for (carryableId <- carryableIds; cell <- players.map(_.universeObject.cell).toSet[Cell]) {
      multiverse.applyGate(
        Gate.X, carryableId, Some("carried"),
        BitControl(id, "alive" -> true),
        PositionControl(id, cell),
        PositionControl(carryableId, cell),
      )
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
  private val position: PositionComponent = add(new PositionComponent(this, cell.toVec2d.add(0.5)))

  private val universeObject: UniverseObject = add(new UniverseObject(this, universe, id, cell))

  private val sprite: DrawableSprite = add(new DrawableSprite(
    entity = this,
    sprite = Sprite.load(getClass.getResource("sprites/cat.png")),
    scale = new Vec2d(2, 2),
    color = WHITE
  ))

  private val bits: BitMap = add(new BitMap(
    this,
    Map("alive" -> true),
    "alive",
    state => sprite.color = if (state("alive")) WHITE else BLACK
  ))

  override def copy(): Player = {
    val player = new Player(universeObject.universe, universeObject.id, universeObject.cell)
    player.position.value = position.value
    player.bits.state = bits.state
    player.layer = layer
    player
  }

  override def draw(): Unit = sprite.draw()
}
