package superposition

import engine.core.Behavior.Entity
import engine.core.Game
import engine.graphics.Graphics.drawWideLine
import engine.graphics.sprites.Sprite
import engine.util.Color
import engine.util.math.Vec2d
import extras.physics.{PositionComponent, Rectangle}

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
 * @param _position the position of this laser
 * @param direction the direction this laser is pointing
 */
private final class Laser(universe: Universe,
                          id: UniversalId,
                          _position: Vec2d,
                          direction: Vec2d) extends Entity with Copyable[Laser] with Drawable {
  private val position: PositionComponent = add(new PositionComponent(this, _position))

  private val universeObject: UniverseObject = add(new UniverseObject(this, universe, id, new Vec2d(1, 1), true))

  private val sprite: DrawableSprite =
    add(new DrawableSprite(this, Sprite.load(getClass.getResource("sprites/cat.png"))))

  private var targetId: Option[UniversalId] = None

  private var targetPoint: Vec2d = _position

  private def step(): Unit = {
    val walls = universeObject.universe.walls.map((_, None))
    val objects = universeObject.universe.objects.values.filter(_.id != id).map(o => (o.hitbox, Some(o.id)))

    // TODO: Update to work with laser directions other than down.
    val beam = new Rectangle(position.value.add(direction.normalize().mul(10)), position.value)
    val hit = (walls ++ objects)
      .filter(_._1.intersects(beam))
      .minBy(_._1.upperRight.sub(position.value).length())

    if (hit._2 != targetId) {
      targetId = hit._2
      targetId match {
        case Some(id) if universeObject.universe.qubits.contains(id) =>
          universeObject.universe.applyGate(Gate.X, id)
        case _ =>
      }
    }
    targetPoint = new Vec2d(position.value.x, hit._1.upperRight.y)
  }

  override def copy(): Laser = new Laser(universeObject.universe, id, position.value, direction)

  override def draw(): Unit = {
    sprite.draw()
    drawWideLine(position.value, targetPoint, 0.25, Color.RED)
  }
}
