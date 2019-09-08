package superposition

import engine.core.Behavior.Entity
import engine.core.Game.dt
import engine.core.{Game, Input}
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
    Game.declareSystem(classOf[Laser], (_: Laser).step())
}

/**
 * A laser applies a quantum gate to any qubit hit by its beam.
 *
 * @param universe  the universe this laser belongs to
 * @param id        the universe object ID for this laser
 * @param cell      the position of this laser
 * @param direction the direction this laser is pointing
 */
private final class Laser(universe: Universe,
                          id: UniversalId,
                          cell: Cell,
                          direction: Direction.Value) extends Entity with Copyable[Laser] with Drawable {
  private val position: PositionComponent =
    add(new PositionComponent(this, new Vec2d(cell.column + 0.5, cell.row + 0.5)))

  private val universeObject: UniverseObject = add(new UniverseObject(this, universe, id, cell, true))

  private val sprite: DrawableSprite =
    add(new DrawableSprite(this, Sprite.load(getClass.getResource("sprites/cat.png"))))

  private var targetCell: Option[Cell] = None
  private var elapsedTime: Double = 0

  override def copy(): Laser = new Laser(universeObject.universe, id, universeObject.cell, direction)

  override def draw(): Unit = {
    sprite.draw()
    if (targetCell.isDefined) {
      drawWideLine(position.value, new Vec2d(targetCell.get.column + 0.5, targetCell.get.row + 0.5), 0.25, Color.RED)
    }
  }

  private def step(): Unit = {
    elapsedTime += dt

    if (targetCell.isEmpty &&
      Input.mouseJustPressed(0) &&
      Cell(Input.mouse().y.floor.toLong, Input.mouse().x.floor.toLong) == universeObject.cell
    ) {
      targetCell = beam.take(50).find(cell =>
        universeObject.multiverse.walls.contains(cell) ||
          universeObject.universe.objects.values.exists(_.cell == cell)
      )
      targetCell.flatMap(cell => universeObject.universe.objects.values.find(_.cell == cell).map(_.id)) match {
        case Some(id) => universeObject.multiverse.applyGate(Gate.X, id)
        case _ =>
      }
      elapsedTime = 0
    } else if (targetCell.isDefined && elapsedTime >= 1) {
      targetCell = None
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
}
