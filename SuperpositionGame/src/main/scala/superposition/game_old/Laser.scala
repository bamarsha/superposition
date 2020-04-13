//package superposition.game_old
//
//import java.lang.Math.min
//import java.net.URL
//
//import engine.core.Behavior.Entity
//import engine.core.Game.dt
//import engine.core.Input
//import engine.graphics.Graphics.{drawRectangleOutline, drawWideLine}
//import engine.graphics.sprites.Sprite
//import engine.util.Color
//import engine.util.Color.RED
//import engine.util.math.{Transformation, Vec2d}
//import extras.physics.PositionComponent
//import superposition.game.SpriteComponent
//import superposition.types.math.{Cell, Direction}
//import superposition.quantum_types.{Multiverse, ObjectId, UniverseObject}
//import superposition.types.quantum.{Gate, PositionControl, Universe}
//
///**
// * Contains initialization for lasers.
// */
//private object Laser {
//  private val MaxNumUniverses: Int = 2
//
//  private val Sprites: Map[Direction.Value, URL] = Map(
//    Direction.Up -> getClass.getResource("sprites/laser_up.png"),
//    Direction.Down -> getClass.getResource("sprites/laser_down.png"),
//    Direction.Left -> getClass.getResource("sprites/laser_left.png"),
//    Direction.Right -> getClass.getResource("sprites/laser_right.png")
//  )
//
//  private val BeamDuration: Double = 0.2
//
//  private val FadeDuration: Double = 0.3
//
//  /**
//   * Declares the laser system.
//   */
//  def declareSystem(): Unit =
//    Multiverse.declareSubsystem(classOf[Laser], step)
//
//  private def step(multiverse: Multiverse, id: ObjectId, lasers: Iterable[Laser]): Unit = {
//    for (laser <- lasers) {
//      if (laser.justActivated) {
//        laser.fire()
//        laser.elapsedTime = 0
//      } else {
//        laser.elapsedTime += dt
//      }
//    }
//
//    val hits =
//      (for (laser <- lasers if laser.justActivated && laser.targetCell.isDefined;
//            targetId <- laser.obj.universe.bitsInCell(laser.targetCell.get)) yield {
//        laser.elapsedTime = 0
//        (laser.targetCell.get, targetId, laser.controlId)
//      }).toSet
//
//    hits.foreach((applyGate(multiverse, lasers.head.gate, lasers.head.control) _).tupled)
//    if (multiverse.universes.length > MaxNumUniverses) {
//      // TODO: Assumes gates are self-adjoint.
//      hits.foreach((applyGate(multiverse, lasers.head.gate, lasers.head.control) _).tupled)
//      // TODO: Show some effect to indicate why the laser isn't working.
//      lasers.withFilter(_.justActivated).foreach(_.targetCell = None)
//    }
//  }
//
//  private def applyGate(multiverse: Multiverse, gate: Gate.Value, controlCell: Option[Cell])
//                       (targetCell: Cell, targetId: ObjectId, controlId: Option[ObjectId]): Unit =
//    controlId match {
//      case Some(controlId) => multiverse.applyGate(
//        gate, targetId, None,
//        PositionControl(targetId, targetCell),
//        BitControl(controlId, "on" -> true),
//        PositionControl(controlId, controlCell.get)
//      )
//      case None => multiverse.applyGate(gate, targetId, None, PositionControl(targetId, targetCell))
//    }
//}
//
///**
// * A laser applies a quantum gate to any qubit hit by its beam.
// *
// * @param universe  the universe this laser belongs to
// * @param id        the universe object ID for this laser
// * @param cell      the position of this laser
// * @param gate      the gate to apply
// * @param direction the direction this laser is pointing
// * @param control   the cell that controls this laser if it contains a bit, or None if the laser is not controlled
// */
//private final class Laser(universe: Universe,
//                          id: ObjectId,
//                          cell: Cell,
//                          private val gate: Gate.Value,
//                          direction: Direction.Value,
//                          private val control: Option[Cell]) extends Entity with Copyable[Laser] with Drawable {
//
//  import Laser._
//
//  private val position: PositionComponent =
//    add(new PositionComponent(this, new Vec2d(cell.column + 0.5, cell.row + 0.5)))
//
//  private val obj: UniverseObject = add(new UniverseObject(this, universe, id, cell, true))
//
//  private val sprite: SpriteComponent = add(new SpriteComponent(this, Sprite.load(Sprites(direction))))
//
//  private var targetCell: Option[Cell] = None
//
//  private var elapsedTime: Double = Double.PositiveInfinity
//
//  override def copy(): Laser = {
//    val laser = new Laser(obj.universe, id, obj.cell, gate, direction, control)
//    laser.targetCell = targetCell
//    laser.elapsedTime = elapsedTime
//    laser.layer = layer
//    laser
//  }
//
//  override def draw(): Unit = {
//    sprite.draw()
//    if (selected) {
//      drawRectangleOutline(Transformation.create(cell.toVec2d, 0, 1), RED)
//    }
//    if (targetCell.isDefined && elapsedTime <= BeamDuration + FadeDuration) {
//      drawWideLine(
//        position.value,
//        new Vec2d(targetCell.get.column + 0.5, targetCell.get.row + 0.5),
//        0.25,
//        new Color(1, 0, 0, min(FadeDuration, BeamDuration + FadeDuration - elapsedTime) / FadeDuration)
//      )
//    }
//  }
//
//  private def fire(): Unit =
//    if (control.isEmpty || controlId.isDefined && obj.universe.bitMaps(controlId.get).state("on")) {
//      targetCell = beam.take(50).find(cell =>
//        obj.multiverse.walls.contains(cell)
//          || obj.universe.objects.values.exists(_.cell == cell)
//      )
//      assert(targetCell.isDefined, "Missing wall in front of laser")
//    } else {
//      targetCell = None
//    }
//
//  private def beam: LazyList[Cell] =
//    LazyList.iterate(obj.cell)(cell =>
//      direction match {
//        case Direction.Up => cell.up
//        case Direction.Down => cell.down
//        case Direction.Left => cell.left
//        case Direction.Right => cell.right
//      }
//    ).tail
//
//  private def controlId: Option[ObjectId] =
//    control.flatMap(
//      obj.universe
//        .bitsInCell(_)
//        .find(obj.universe.bitMaps(_).state.contains("on"))
//    )
//
//  private def selected: Boolean =
//    Cell(Input.mouse().y.floor.toInt, Input.mouse().x.floor.toInt) == obj.cell
//
//  private def justActivated: Boolean =
//    Input.mouseJustPressed(0) && selected
//}
