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
                          direction: Vec2d) extends Entity with Copyable[Laser] {
  private val position: PositionComponent = add(new PositionComponent(this, _position))

  add(new UniverseObject(this, universe, id, new Vec2d(1, 1), true))

  add(new Drawable(this, Sprite.load(getClass.getResource("sprites/cat.png"))))

  private var target: Option[UniversalId] = None

  private def step(): Unit = {
    val walls = universe.walls.map((_, None))
    val objects = universe.objects.values.filter(_.id != id).map(o => (o.hitbox, Some(o.id)))

    // TODO: Update to work with laser directions other than down.
    val beam = new Rectangle(position.value.add(direction.normalize().mul(10)), position.value)
    val hit = (walls ++ objects)
      .filter(_._1.intersects(beam))
      .minBy(_._1.upperRight.sub(position.value).length())

    if (hit._2 != target) {
      target = hit._2
      target match {
        case Some(id) if universe.qubits.contains(id) => universe.applyGate(Gate.X, id)
        case _ =>
      }
    }
    drawWideLine(position.value, new Vec2d(position.value.x, hit._1.upperRight.y), 0.25, Color.RED)
  }

  override def copy(): Laser = new Laser(universe, id, position.value, direction)
}
