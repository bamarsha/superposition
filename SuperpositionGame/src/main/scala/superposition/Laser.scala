package superposition

import engine.core.Behavior.Entity
import engine.core.Game.dt
import engine.core.Input
import engine.graphics.Graphics.drawWideLine
import engine.graphics.sprites.Sprite
import engine.util.Color
import engine.util.math.Vec2d
import extras.physics.PositionComponent

/**
 * Contains initialization for lasers.
 */
private object Laser {
  /**
   * Declares the laser system.
   */
  def declareSystem(): Unit =
    Multiverse.declareSubsystem(classOf[Laser], step)

  private def step(multiverse: Multiverse, id: UniversalId, lasers: Iterable[Laser]): Unit = {
    lasers.foreach(_.elapsedTime += dt)

    val laser = lasers.head
    if (lasers.exists(_.justFired)) {
      // TODO: Search the laser beam for each universe because the target cells might be different.
      val targetCell = laser.beam.take(50).find(cell =>
        multiverse.walls.contains(cell) ||
          lasers.exists(_.universeObject.universe.objects.values.exists(_.cell == cell))
      )
      lasers.foreach(_.targetCell = targetCell)
      if (targetCell.isDefined) {
        for (targetId <- multiverse.bitsInCell(targetCell.get)) {
          if (laser.control.isEmpty) {
            multiverse.applyGate(laser.gate, targetId, None, PositionControl(targetId, targetCell.get))
          } else {
            // TODO: Make sure the target and control aren't the same.
            val controlId = multiverse.bitsInCell(laser.control.get).head
            multiverse.applyGate(
              laser.gate, targetId, None,
              PositionControl(targetId, targetCell.get),
              BitControl(controlId, "on" -> true),
              PositionControl(controlId, laser.control.get)
            )
          }
        }
      }
      lasers.foreach(_.elapsedTime = 0)
    } else if (laser.targetCell.isDefined && laser.elapsedTime >= 1) {
      lasers.foreach(_.targetCell = None)
    }
  }
}

/**
 * A laser applies a quantum gate to any qubit hit by its beam.
 *
 * @param universe  the universe this laser belongs to
 * @param id        the universe object ID for this laser
 * @param cell      the position of this laser
 * @param gate      the gate to apply
 * @param direction the direction this laser is pointing
 * @param control   the cell that controls this laser if it contains a bit, or None if the laser is not controlled
 */
private final class Laser(universe: Universe,
                          id: UniversalId,
                          cell: Cell,
                          private val gate: Gate.Value,
                          direction: Direction.Value,
                          private val control: Option[Cell]) extends Entity with Copyable[Laser] with Drawable {
  private val position: PositionComponent =
    add(new PositionComponent(this, new Vec2d(cell.column + 0.5, cell.row + 0.5)))

  private val universeObject: UniverseObject = add(new UniverseObject(this, universe, id, cell, true))

  private val sprite: DrawableSprite =
    add(new DrawableSprite(this, Sprite.load(getClass.getResource("sprites/cat.png"))))

  private var targetCell: Option[Cell] = None
  private var elapsedTime: Double = 0

  override def copy(): Laser = new Laser(universeObject.universe, id, universeObject.cell, gate, direction, control)

  override def draw(): Unit = {
    sprite.draw()
    if (targetCell.isDefined && controlBitIsOn) {
      drawWideLine(position.value, new Vec2d(targetCell.get.column + 0.5, targetCell.get.row + 0.5), 0.25, Color.RED)
    }
  }

  private def beam: LazyList[Cell] =
    LazyList.iterate(universeObject.cell)(cell =>
      direction match {
        case Direction.Up => cell.up
        case Direction.Down => cell.down
        case Direction.Left => cell.left
        case Direction.Right => cell.right
      }
    ).tail

  private def controlBitIsOn: Boolean =
    control.isEmpty || (universeObject.universe.bitsInCell(control.get).headOption match {
      case Some(id) => universeObject.universe.bits(id).state.get("on").contains(true)
      case _ => false
    })

  private def justFired: Boolean =
    targetCell.isEmpty &&
      Input.mouseJustPressed(0) &&
      Cell(Input.mouse().y.floor.toLong, Input.mouse().x.floor.toLong) == universeObject.cell &&
      controlBitIsOn
}
