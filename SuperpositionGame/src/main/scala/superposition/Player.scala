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

  private var relativePosition = new Vec2d(0.5, 0.5)

  /**
   * Declares the player system.
   */
  def declareSystem(): Unit =
    Multiverse.declareSubsystem(classOf[Player], step)

  private def step(multiverse: Multiverse, id: UniversalId, players: Iterable[Player]): Unit = {
    move(multiverse, id, players)

    if (Input.keyJustPressed(GLFW_KEY_SPACE)) {
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

  // TODO: Refactor this method.
  private def move(multiverse: Multiverse, id: UniversalId, players: Iterable[Player]): Unit = {
    val delta = WalkKeys.foldLeft(new Vec2d(0, 0)) {
      case (delta, (key, direction)) => if (Input.keyDown(key)) delta add direction else delta
    }
    relativePosition = relativePosition add (if (delta.length == 0) new Vec2d(0, 0) else delta.normalize mul 6.5 * dt)

    if (relativePosition.x < -1e-3) {
      if (walk(multiverse, id, Gate.Left, players)) {
        relativePosition = relativePosition.add(new Vec2d(1, 0))
      } else {
        relativePosition = relativePosition.setX(0)
      }
    }
    if (relativePosition.x > 1 + 1e-3) {
      if (walk(multiverse, id, Gate.Right, players)) {
        relativePosition = relativePosition.add(new Vec2d(-1, 0))
      } else {
        relativePosition = relativePosition.setX(1)
      }
    }
    if (relativePosition.y < -1e-3) {
      if (walk(multiverse, id, Gate.Down, players)) {
        relativePosition = relativePosition.add(new Vec2d(0, 1))
      } else {
        relativePosition = relativePosition.setY(0)
      }
    }
    if (relativePosition.y > 1 + 1e-3) {
      if (walk(multiverse, id, Gate.Up, players)) {
        relativePosition = relativePosition.add(new Vec2d(0, -1))
      } else {
        relativePosition = relativePosition.setY(1)
      }
    }

    for (p <- players) {
      val newPos = p.universeObject.cell.toVec2d.add(if (p.bits.state("alive")) relativePosition else new Vec2d(.5, .5))
      if (p.bits.state("alive")) {
        p.position.value = p.position.value.lerp(newPos, 10 * dt);
      }
    }
    for (o <- players.flatMap(_.universeObject.universe.bits.values.filter(_.state.contains("carried")))) {
      val newPos = o.universeObject.cell.toVec2d.add(if (o.state("carried")) relativePosition else new Vec2d(.5, .5))
      o.universeObject.position.value = o.universeObject.position.value.lerp(newPos, 10 * dt);
    }
  }

  private def walk(multiverse: Multiverse, id: UniversalId, gate: Gate.Value, players: Iterable[Player]): Boolean = {
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
  private val position: PositionComponent = add(new PositionComponent(this, cell.toVec2d.add(.5)))

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
    player
  }

  override def draw(): Unit = sprite.draw()
}
