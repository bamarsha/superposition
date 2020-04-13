package superposition.game

import engine.core.Behavior.Entity
import engine.core.Game.{dt, track}
import engine.core.{Game, Input}
import engine.graphics.sprites.Sprite
import engine.util.Color.WHITE
import engine.util.math.Vec2d
import extras.physics.PositionComponent
import org.lwjgl.glfw.GLFW._
import superposition.types.math.Cell
import superposition.types.quantum.{Gate, Id}

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
 * @param multiverse the multiverse this player belongs to
 * @param cell       the initial position for this player
 */
class Player(multiverse: Multiverse, cell: Cell) extends Entity {

  val position: PositionComponent = add(new PositionComponent(this, cell.toVec2d.add(0.5)))

  val sprite: SpriteComponent = add(new SpriteComponent(this, Player.CatSprite, new Vec2d(2, 2), WHITE))

  val alive: Id[Boolean] = multiverse.createId(true)
  val qPosition: Id[Cell] = multiverse.createId(cell)

  val universe: UniverseComponent = add(new UniverseComponent(this, multiverse))
  universe.primaryBit = Some(alive)
  universe.position = Some(qPosition)

  private var cellPosition = new Vec2d(0.5, 0.5)

  private def step(): Unit = {
    cellPosition = cellPositionFromInput
    val (x, y) = walkGates()
    if (walk(x, y)) {
      cellPosition = cellPosition.sub(new Vec2d(x, y))
    }
    cellPosition = cellPosition.clamp(0, 1)

    updateAbsolutePositions()
    if (Input.keyJustPressed(GLFW_KEY_SPACE)) {
      toggleCarrying()
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

  private def updateAbsolutePositions(): Unit = {
    position.value = position.value.lerp(multiverse.universes(0).get(qPosition).toVec2d add cellPosition, 10 * dt)
//    val livePlayers = players.filter(_.bits.state("alive"))
//    for (player <- livePlayers) {
//      val position = player.position
//      val cell = player.obj.cell
//      position.value = position.value.lerp(cell.toVec2d add cellPosition, 10 * dt)
//    }
//    for (bits <- livePlayers.flatMap(_.obj.universe.bitMaps.values)
//         if bits.state.contains("carried")) {
//      val position = bits.obj.position
//      val target = if (bits.state("carried")) cellPosition else new Vec2d(0.5, 0.5)
//      val cell = bits.obj.cell
//      position.value = position.value.lerp(cell.toVec2d add target, 10 * dt)
//    }
  }

  private def walk(x: Int, y: Int): Boolean = {

    val translatePlayer: Gate[(Int, Int)] = Gate.control[(Int, Int), List[(Id[Cell], Int, Int)]](
      { case (x:Int, y:Int) => u => if (u.get(alive))
      List((qPosition, x, y)) else List() } )(Gate.multi(Gate.translate))
    val translateQuballs: Gate[(Int, Int)] = Gate.control[(Int, Int), List[(Id[Cell], Int, Int)]](
      { case (x, y) => u => if (u.get(alive))
      Quball.All.filter(q => u.get(q.carried)).map(q => (q.qPosition, x, y)).toList
    else List() } )(Gate.multi(Gate.translate))

    val endGate = Gate.compose(List(translatePlayer, translateQuballs))

    multiverse.applyGate(endGate, (x, y))
  }

  private def toggleCarrying(): Boolean = {
    val gate: Gate[Unit] = Gate.control(
      (_: Unit) => u => Quball.All.filter(q => u.get(alive) && u.get(qPosition) == u.get(q.qPosition))
      .map(_.carried).toList)(Gate.multi(Gate.X))
    multiverse.applyGate(gate, ())

//    val carryableIds = players.flatMap(player => {
//      val universe = player.obj.universe
//      universe
//        .bitsInCell(player.obj.cell)
//        .filter(otherId => otherId != id && universe.bitMaps(otherId).state.contains("carried"))
//    }).toSet
//    for (carryableId <- carryableIds; cell <- players.map(_.obj.cell).toSet[Cell]) {
//      multiverse.applyGate(
//        Gate.X, carryableId, Some("carried"),
//        BitControl(id, "alive" -> true),
//        PositionControl(id, cell),
//        PositionControl(carryableId, cell),
//      )
//    }
  }
}
