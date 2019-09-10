package superposition

import java.lang.Math.min
import java.net.URL

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
  private val Sprites: Map[Direction.Value, URL] = Map(
    Direction.Up -> getClass.getResource("sprites/laser_up.png"),
    Direction.Left -> getClass.getResource("sprites/laser_left.png")
  )

  private val BeamDuration: Double = 1

  private val FadeDuration: Double = 0.2

  /**
   * Declares the laser system.
   */
  def declareSystem(): Unit =
    Multiverse.declareSubsystem(classOf[Laser], step)

  private def step(multiverse: Multiverse, id: UniversalId, lasers: Iterable[Laser]): Unit = {
    for (laser <- lasers) {
      if (laser.justClicked) {
        laser.fire()
        laser.elapsedTime = 0
      } else {
        laser.elapsedTime += dt
      }
    }

    val hits = for (laser <- lasers if laser.justClicked && laser.targetCell.isDefined;
                    targetId <- laser.universeObject.universe.bitsInCell(laser.targetCell.get)) yield {
      laser.elapsedTime = 0
      (laser.targetCell.get, targetId, laser.controlId)
    }
    for ((targetCell, targetId, controlId) <- hits.toSet) {
      controlId match {
        case Some(controlId) =>
          multiverse.applyGate(
            lasers.head.gate, targetId, None,
            PositionControl(targetId, targetCell),
            BitControl(controlId, "on" -> true),
            PositionControl(controlId, lasers.head.control.get)
          )
        case None => multiverse.applyGate(lasers.head.gate, targetId, None, PositionControl(targetId, targetCell))
      }
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

  import Laser._

  private val position: PositionComponent =
    add(new PositionComponent(this, new Vec2d(cell.column + 0.5, cell.row + 0.5)))

  private val universeObject: UniverseObject = add(new UniverseObject(this, universe, id, cell, true))

  private val sprite: DrawableSprite = add(new DrawableSprite(this, Sprite.load(Sprites(direction))))

  private var targetCell: Option[Cell] = None

  private var elapsedTime: Double = Double.PositiveInfinity

  override def copy(): Laser = new Laser(universeObject.universe, id, universeObject.cell, gate, direction, control)

  override def draw(): Unit = {
    sprite.draw()
    if (targetCell.isDefined && elapsedTime <= BeamDuration + FadeDuration) {
      drawWideLine(
        position.value,
        new Vec2d(targetCell.get.column + 0.5, targetCell.get.row + 0.5),
        0.25,
        new Color(255, 0, 0, min(FadeDuration, BeamDuration + FadeDuration - elapsedTime) / FadeDuration)
      )
    }
  }

  private def fire(): Set[UniversalId] =
    if (control.isEmpty || controlId.isDefined && universeObject.universe.bits(controlId.get).state("on")) {
      targetCell = beam.take(50).find(cell =>
        universeObject.multiverse.walls.contains(cell)
          || universeObject.universe.objects.values.exists(_.cell == cell)
      )
      assert(targetCell.isDefined, "Missing wall in front of laser")
      universeObject.universe.bitsInCell(targetCell.get)
    } else {
      targetCell = None
      Set.empty
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

  private def controlId: Option[UniversalId] =
    control.flatMap(
      universeObject.universe
        .bitsInCell(_)
        .find(universeObject.universe.bits(_).state.contains("on"))
    )

  private def justClicked: Boolean =
    Input.mouseJustPressed(0) &&
      Cell(Input.mouse().y.floor.toLong, Input.mouse().x.floor.toLong) == universeObject.cell
}
