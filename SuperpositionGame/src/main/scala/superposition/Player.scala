package superposition

import engine.core.Behavior.Entity
import engine.core.Game.dt
import engine.core.Input
import engine.graphics.sprites.Sprite
import engine.util.Color.{BLACK, WHITE}
import engine.util.math.Vec2d
import extras.physics.PositionComponent
import org.lwjgl.glfw.GLFW._

import scala.collection.immutable.HashMap

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

  var fractionalPos = new Vec2d(.5, .5)

  private def step(multiverse: Multiverse, id: UniversalId, players: Iterable[Player]): Unit = {
    var diff = new Vec2d(0, 0)
    if (Input.keyDown(GLFW_KEY_A)) {
      diff = diff.add(new Vec2d(-1, 0))
    }
    if (Input.keyDown(GLFW_KEY_D)) {
      diff = diff.add(new Vec2d(1, 0))
    }
    if (Input.keyDown(GLFW_KEY_S)) {
      diff = diff.add(new Vec2d(0, -1))
    }
    if (Input.keyDown(GLFW_KEY_W)) {
      diff = diff.add(new Vec2d(0, 1))
    }
    fractionalPos = fractionalPos.add(diff.mul(6.5 * dt()))

    if (fractionalPos.x < -1e-3) {
      if (walk(multiverse, id, Gate.Left, players)) {
        fractionalPos = fractionalPos.add(new Vec2d(1, 0))
      } else {
        fractionalPos = fractionalPos.setX(0)
      }
    }
    if (fractionalPos.x > 1 + 1e-3) {
      if (walk(multiverse, id, Gate.Right, players)) {
        fractionalPos = fractionalPos.add(new Vec2d(-1, 0))
      } else {
        fractionalPos = fractionalPos.setX(1)
      }
    }
    if (fractionalPos.y < -1e-3) {
      if (walk(multiverse, id, Gate.Down, players)) {
        fractionalPos = fractionalPos.add(new Vec2d(0, 1))
      } else {
        fractionalPos = fractionalPos.setY(0)
      }
    }
    if (fractionalPos.y > 1 + 1e-3) {
      if (walk(multiverse, id, Gate.Up, players)) {
        fractionalPos = fractionalPos.add(new Vec2d(0, -1))
      } else {
        fractionalPos = fractionalPos.setY(1)
      }
    }
    for (p <- players) {
      val newPos = p.universeObject.cell.toVec2d.add(if (p.bits.state("alive")) fractionalPos else new Vec2d(.5, .5))
      if (p.bits.state("alive")) {
        p.position.value = p.position.value.lerp(newPos, 10 * dt);
      }
    }
    for (o <- players.flatMap(_.universeObject.universe.bits.values.filter(_.state.contains("carried")))) {
      val newPos = o.universeObject.cell.toVec2d.add(if (o.state("carried")) fractionalPos else new Vec2d(.5, .5))
      o.universeObject.position.value = o.universeObject.position.value.lerp(newPos, 10 * dt);
    }

    if (Input.keyJustPressed(GLFW_KEY_SPACE)) {
      val carryIds = players.flatMap(player => {
        val universe = player.universeObject.universe
        universe
          .bitsInCell(player.universeObject.cell)
          .filter(otherId => otherId != id && universe.bits(otherId).state.contains("carried"))
      }).toSet
      for (carryId <- carryIds; cell <- players.map(_.universeObject.cell).toSet[Cell]) {
        multiverse.applyGate(
          Gate.X, carryId, Some("carried"),
          BitControl(id, "alive" -> true),
          PositionControl(carryId, cell)
        )
      }
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
      players.foreach(_.timeSinceLastWalk = 0)
      return true
    }
    false
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
  val position: PositionComponent = add(new PositionComponent(this, cell.toVec2d.add(.5)))

  val universeObject: UniverseObject = add(new UniverseObject(this, universe, id, cell))

  val sprite: DrawableSprite = add(new DrawableSprite(
    entity = this,
    sprite = Sprite.load(getClass.getResource("sprites/cat.png")),
    scale = new Vec2d(2, 2),
    color = WHITE
  ))

  private val bits: BitMap = add(new BitMap(
    this,
    HashMap("alive" -> true),
    "alive",
    state => sprite.color = if (state("alive")) WHITE else BLACK
  ))

  private var timeSinceLastWalk = Double.PositiveInfinity

  override def copy(): Player = {
    val player = new Player(universeObject.universe, universeObject.id, universeObject.cell)
    player.position.value = position.value
    player.bits.state = bits.state
    player
  }

  override def draw(): Unit = sprite.draw()
}
