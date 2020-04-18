package superposition.game

import engine.core.Behavior.Entity
import engine.core.Game.{dt, track}
import engine.core.{Game, Input}
import engine.graphics.sprites.Sprite
import engine.util.Color._
import engine.util.math.Vec2d
import org.lwjgl.glfw.GLFW._
import superposition.math.Vec2i
import superposition.quantum._

import scala.Function.const
import scala.jdk.CollectionConverters._

/**
 * Contains settings and initialization for the player.
 */
object Player {
  private val WalkKeys: Map[Int, Vec2d] = Map(
    GLFW_KEY_W -> new Vec2d(0, 1),
    GLFW_KEY_A -> new Vec2d(-1, 0),
    GLFW_KEY_S -> new Vec2d(0, -1),
    GLFW_KEY_D -> new Vec2d(1, 0)
  )

  /**
   * Declares the player system.
   */
  def declareSystem(): Unit = Game.declareSystem(classOf[Player], (_: Player).step())

  val All: Iterable[Player] = track(classOf[Player]).asScala
  private val CatSprite = Sprite.load(getClass.getResource("sprites/cat.png"))
}

/**
 * The player character in the game.
 *
 * @param multiverse  the multiverse this player belongs to
 * @param initialCell the initial position for this player
 */
class Player(multiverse: Multiverse, initialCell: Vec2i) extends Entity {

  // Quantum state
  val alive: StateId[Boolean] = multiverse.allocate(true)
  val cell: StateId[Vec2i] = multiverse.allocate(initialCell)

  // Metadata
  val position: MetaId[Vec2d] = multiverse.allocateMeta(initialCell.toVec2d.add(.5))

  val sprite: SpriteComponent = add(new SpriteComponent(this,
    _ => Player.CatSprite, _.meta(position), _ => new Vec2d(2, 2), u => if (u.state(alive)) WHITE else BLACK))

  val universe: UniverseComponent = add(new UniverseComponent(this, multiverse))
  universe.primaryBit = Some(alive)
  universe.position = Some(cell)

  var cellPosition = new Vec2d(0.5, 0.5)

  private def step(): Unit = {
    cellPosition = cellPositionFromInput
    val (x, y) = walkGates()
    if (walk(x, 0)) cellPosition = cellPosition.sub(new Vec2d(x, 0))
    if (walk(0, y)) cellPosition = cellPosition.sub(new Vec2d(0, y))
    cellPosition = cellPosition.clamp(0, 1)

    if (Input.keyJustPressed(GLFW_KEY_SPACE)) {
      toggleCarrying()
    }

    multiverse.updateMetaWith(position) { pos => universe =>
      if (universe.state(alive)) {
        val targetPosition = universe.state(cell).toVec2d add cellPosition
        pos.lerp(targetPosition, 10 * dt)
      } else pos
    }

    for (quball <- Quball.All) {
      multiverse.updateMetaWith(quball.position) { pos => universe =>
        val relativePos = if (universe.state(quball.carried)) cellPosition else new Vec2d(0.5, 0.5)
        val targetPos = universe.state(quball.cell).toVec2d add relativePos
        if (universe.state(alive)) pos.lerp(targetPos, 10 * dt) else pos
      }
    }
  }

  private def cellPositionFromInput: Vec2d = {
    val delta = Player.WalkKeys.foldLeft(new Vec2d(0, 0)) {
      case (delta, (key, direction)) => if (Input.keyDown(key)) delta add direction else delta
    }
    cellPosition add (if (delta.length == 0) new Vec2d(0, 0) else delta.setLength(6.5 * dt))
  }

  private def walkGates(): (Int, Int) = {
    var x = 0
    var y = 0
    if (cellPosition.x < -1e-3) x -= 1
    if (cellPosition.x > 1 + 1e-3) x += 1
    if (cellPosition.y < -1e-3) y -= 1
    if (cellPosition.y > 1 + 1e-3) y += 1
    (x, y)
  }

  private def walk(x: Int, y: Int): Boolean =
    if (x == 0 && y == 0) {
      true
    } else {
      val translatePlayer: Gate[Vec2i] = Translate.multi control {
        delta => universe => if (universe.state(alive)) List((cell, delta)) else List()
      }
      val translateQuballs: Gate[Vec2i] = Translate.multi control {
        delta => universe =>
          if (universe.state(alive))
            (Quball.All
              filter (quball => universe.state(quball.carried))
              map (quball => (quball.cell, delta)))
              .toList
          else List()
      }
      multiverse.applyGate(translatePlayer andThen translateQuballs, Vec2i(x, y))
    }

  private def toggleCarrying(): Boolean =
    multiverse.applyGate[Unit](X.multi control const {
      universe =>
        (Quball.All
          filter (quball => universe.state(alive) && universe.state(cell) == universe.state(quball.cell))
          map (_.carried))
          .toList
    }, ())
}
