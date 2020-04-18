package superposition.game

import engine.core.Behavior.Entity
import engine.core.Game
import engine.core.Game.{dt, track}
import engine.core.Input.{keyDown, keyJustPressed}
import engine.graphics.sprites.Sprite
import engine.util.Color._
import engine.util.math.Vec2d
import org.lwjgl.glfw.GLFW._
import superposition.game.Player.{CatSprite, deltaPosition, snapPosition}
import superposition.math.Vec2i
import superposition.quantum._

import scala.Function.const
import scala.jdk.CollectionConverters._

/**
 * The player character in the game.
 *
 * @param multiverse  the multiverse this player belongs to
 * @param initialCell the initial position for this player
 */
class Player(multiverse: Multiverse, initialCell: Vec2i) extends Entity {
  private val alive: StateId[Boolean] = multiverse.allocate(true)

  val cell: StateId[Vec2i] = multiverse.allocate(initialCell)

  private val position: MetaId[Vec2d] = multiverse.allocateMeta(initialCell.toVec2d add 0.5)

  private var relativePosition = new Vec2d(0.5, 0.5)

  add(new SpriteComponent(this,
    sprite = const(CatSprite),
    position = _.meta(position),
    scale = const(new Vec2d(2, 2)),
    universe => if (universe.state(alive)) WHITE else BLACK))

  add(new UniverseComponent(this, primaryBit = Some(alive), position = Some(cell)))

  private val walkGate: Gate[Vec2i] = {
    val movePlayer: Gate[Vec2i] = Translate.multi control { delta => universe =>
      if (universe.state(alive)) List((cell, delta)) else List()
    }
    val moveQuballs: Gate[Vec2i] = Translate.multi control { delta => universe =>
      if (universe.state(alive))
        Quball.All
          .filter(quball => universe.state(quball.carried))
          .map(quball => (quball.cell, delta))
          .toList
      else List()
    }
    movePlayer andThen moveQuballs
  }

  private val carryGate: Gate[Unit] = X.multi control const { universe =>
    Quball.All
      .filter(quball => universe.state(alive) && universe.state(cell) == universe.state(quball.cell))
      .map(_.carried)
      .toList
  }

  private def walkIntent(): Vec2i = {
    val next = relativePosition add deltaPosition()
    Vec2i(snapPosition(next.x), snapPosition(next.y))
  }

  private def walk(): Unit = {
    def applyGate(delta: Vec2i) = multiverse.applyGate(walkGate, delta)

    val Vec2i(dx, dy) = walkIntent()
    val delta = deltaPosition() add new Vec2d(
      if (dx != 0 && applyGate(Vec2i(dx, 0))) -dx else 0,
      if (dy != 0 && applyGate(Vec2i(0, dy))) -dy else 0)
    relativePosition = (relativePosition add delta).clamp(0, 1)
  }

  private def updatePosition(): Unit = {
    multiverse.updateMetaWith(position) { pos => universe =>
      if (universe.state(alive)) {
        val targetPosition = universe.state(cell).toVec2d add relativePosition
        pos.lerp(targetPosition, 10 * dt)
      } else pos
    }

    for (quball <- Quball.All) {
      multiverse.updateMetaWith(quball.position) { pos => universe =>
        val relativePos = if (universe.state(quball.carried)) relativePosition else new Vec2d(0.5, 0.5)
        val targetPos = universe.state(quball.cell).toVec2d add relativePos
        if (universe.state(alive)) pos.lerp(targetPos, 10 * dt) else pos
      }
    }
  }

  private def step(): Unit = {
    walk()
    if (keyJustPressed(GLFW_KEY_SPACE)) {
      multiverse.applyGate(carryGate, ())
    }
    updatePosition()
  }
}

object Player {
  val All: Iterable[Player] = track(classOf[Player]).asScala

  private val WalkKeys: Map[Int, Vec2d] = Map(
    GLFW_KEY_W -> new Vec2d(0, 1),
    GLFW_KEY_A -> new Vec2d(-1, 0),
    GLFW_KEY_S -> new Vec2d(0, -1),
    GLFW_KEY_D -> new Vec2d(1, 0))

  private val CatSprite: Sprite = Sprite.load(getClass.getResource("sprites/cat.png"))

  private val Speed: Double = 6.5

  def declareSystem(): Unit = Game.declareSystem(classOf[Player], (_: Player).step())

  private def deltaPosition(): Vec2d = {
    val delta = WalkKeys.foldLeft(new Vec2d(0, 0)) {
      case (delta, (key, direction)) => if (keyDown(key)) delta add direction else delta
    }
    if (delta.length == 0) delta else delta.setLength(Speed * dt)
  }

  private def snapPosition(delta: Double): Int =
    if (delta < -1e-3) -1
    else if (delta > 1 + 1e-3) 1
    else 0
}
