package superposition

import engine.core.Behavior.Entity
import engine.core.Input
import engine.graphics.sprites.Sprite
import engine.util.Color.WHITE
import engine.util.math.Vec2d
import extras.physics.PhysicsComponent
import org.lwjgl.glfw.GLFW._

import scala.jdk.CollectionConverters._

private object Player {
  private val Speed: Double = 7.5

  private val WalkKeys: List[(Int, Vec2d)] = List(
    (GLFW_KEY_W, new Vec2d(0, 1)),
    (GLFW_KEY_A, new Vec2d(-1, 0)),
    (GLFW_KEY_S, new Vec2d(0, -1)),
    (GLFW_KEY_D, new Vec2d(1, 0))
  )

  private def walkVelocity(): Vec2d = {
    val direction = WalkKeys.foldLeft(new Vec2d(0, 0)) {
      case (acc, (key, direction)) => if (Input.keyDown(key)) acc.add(direction) else acc
    }
    if (direction.length() == 0) direction else direction.setLength(Speed)
  }
}

/**
 * The player character in the game.
 *
 * @param universe the universe this player belongs to
 * @param id the universe object ID for this player
 * @param position the initial position for this player
 */
private class Player(universe: Universe, id: UniversalId, position: Vec2d) extends Entity with Copyable[Player] {
  import Player._

  private val physics: PhysicsComponent = add(new PhysicsComponent(
    this,
    position,
    new Vec2d(0, 0),
    PhysicsComponent.wallCollider(new Vec2d(1.8, 1.8), universe.walls.map(_.rectangle).asJavaCollection)
  ))

  add(new Drawable(
    entity = this,
    sprite = Sprite.load(getClass.getResource("sprites/cat.png")),
    scale = new Vec2d(2, 2),
    color = WHITE
  ))

  private val universeObject: UniverseObject = add(new UniverseObject(this, universe, id))

  private var carrying: Option[UniversalId] = None

  /**
   * Steps time forward for this player.
   */
  def step(): Unit = {
    physics.velocity = walkVelocity()
    if (Input.keyJustPressed(GLFW_KEY_SPACE)) {
      toggleCarrying()
    }
    for (id <- carrying) {
      universeObject.universe.objects(id).physics.position = physics.position
      universeObject.universe.objects(id).physics.velocity = physics.velocity
    }
  }

  private def toggleCarrying(): Unit =
    if (carrying.isEmpty)
      carrying = universeObject.universe.objects.values
        .find(o => o.entity != this && o.physics.position.sub(physics.position).length() < 1)
        .map(_.id)
    else
      carrying = None

  override def copy(): Player = {
    val player = new Player(universeObject.universe, universeObject.id, physics.position)
    player.carrying = carrying
    player
  }
}
