package superposition.game

import engine.core.Behavior.Entity
import engine.core.Game.{dt, track}
import engine.core.{Game, Input}
import engine.graphics.sprites.Sprite
import engine.util.math.Vec2d
import org.lwjgl.glfw.GLFW._
import superposition.math.Cell
import superposition.quantum.{Gate, Id}
import engine.util.Color._

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
class Player(multiverse: Multiverse, initialCell: Cell) extends Entity {

  // Quantum state
  val alive: Id[Boolean] = multiverse.createId(true)
  val cell: Id[Cell] = multiverse.createId(initialCell)

  // Metadata
  val position: Id[Vec2d] = multiverse.createIdMeta(initialCell.toVec2d.add(.5))

  val sprite: SpriteComponent = add(new SpriteComponent(this,
    _ => Player.CatSprite, _.getMeta(position), _ => new Vec2d(2, 2), u => if (u.get(alive)) WHITE else BLACK))

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

    if (Input.keyJustPressed(GLFW_KEY_SPACE)) toggleCarrying()

    multiverse.universes = multiverse.universes.map(u => {
      if (u.get(alive)) {
        val desiredPos = u.get(cell).toVec2d.add(cellPosition)
        u.setMeta(position)(u.getMeta(position).lerp(desiredPos, 10 * dt))
      } else u
    })

    Quball.All.foreach(q => multiverse.universes = multiverse.universes.map(u => {
      val desiredPos = u.get(q.cell).toVec2d.add(if (u.get(q.carried)) cellPosition else new Vec2d(.5, .5))
      u.setMeta(q.position)(u.getMeta(q.position).lerp(desiredPos, 10 * dt))
    }))
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

  private def walk(x: Int, y: Int): Boolean = {

    val translatePlayer: Gate[(Int, Int)] = Gate.control[(Int, Int), List[(Id[Cell], Int, Int)]](
      { case (x:Int, y:Int) => u => if (u.get(alive))
      List((cell, x, y)) else List() } )(Gate.multi(Gate.translate))
    val translateQuballs: Gate[(Int, Int)] = Gate.control[(Int, Int), List[(Id[Cell], Int, Int)]](
      { case (x, y) => u => if (u.get(alive))
      Quball.All.filter(q => u.get(q.carried)).map(q => (q.cell, x, y)).toList
    else List() } )(Gate.multi(Gate.translate))

    val endGate = Gate.compose(List(translatePlayer, translateQuballs))

    multiverse.applyGate(endGate, (x, y))
  }

  private def toggleCarrying(): Boolean = {
    val gate: Gate[Unit] = Gate.control(
      (_: Unit) => u => Quball.All.filter(q => u.get(alive) && u.get(cell) == u.get(q.cell))
      .map(_.carried).toList)(Gate.multi(Gate.X))
    multiverse.applyGate(gate, ())
  }
}
