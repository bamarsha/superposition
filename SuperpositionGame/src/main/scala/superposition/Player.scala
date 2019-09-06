package superposition

import engine.core.Behavior.Entity
import engine.core.{Game, Input}
import engine.graphics.sprites.Sprite
import engine.util.Color.WHITE
import engine.util.math.Vec2d
import extras.physics.{PhysicsComponent, PositionComponent}
import org.lwjgl.glfw.GLFW._

/**
 * Contains settings and initialization for the player.
 */
private object Player {
  private val Speed: Double = 7.5

  private val WalkKeys: List[(Int, Vec2d)] = List(
    (GLFW_KEY_W, new Vec2d(0, 1)),
    (GLFW_KEY_A, new Vec2d(-1, 0)),
    (GLFW_KEY_S, new Vec2d(0, -1)),
    (GLFW_KEY_D, new Vec2d(1, 0))
  )

  /**
   * Declares the player system.
   */
  def declareSystem(): Unit =
    Game.declareSystem(classOf[Player], (_: Player).step())

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
 * @param universe  the universe this player belongs to
 * @param id        the universe object ID for this player
 * @param _position the initial position for this player
 */
private final class Player(universe: Universe,
                           id: UniversalId,
                           _position: Vec2d) extends Entity with Copyable[Player] with Drawable {

  import Player._

  private val position: PositionComponent = add(new PositionComponent(this, _position))

  private val universeObject: UniverseObject = add(new UniverseObject(this, universe, id, new Vec2d(1.8, 1.8)))

  private val physics: PhysicsComponent = add(new PhysicsComponent(this, new Vec2d(0, 0), universeObject.collides))

  val sprite: DrawableSprite = add(new DrawableSprite(
    entity = this,
    sprite = Sprite.load(getClass.getResource("sprites/cat.png")),
    scale = new Vec2d(2, 2),
    color = WHITE
  ))

  private var carrying: Option[UniversalId] = None

  private def step(): Unit = {
    physics.velocity = walkVelocity()
    if (Input.keyJustPressed(GLFW_KEY_SPACE)) {
      toggleCarrying()
    }
    for (id <- carrying) {
      universeObject.universe.physicsObjects(id).position.value = physics.position.value
      universeObject.universe.physicsObjects(id).velocity = physics.velocity
    }
  }

  private def toggleCarrying(): Unit =
    if (carrying.isEmpty)
      carrying = universeObject.universe.physicsObjects
        .find(o => o._2.entity != this && o._2.position.value.sub(position.value).length() < 1)
        .map(_._1)
    else
      carrying = None

  override def copy(): Player = {
    val player = new Player(universeObject.universe, universeObject.id, position.value)
    player.carrying = carrying
    player
  }

  override def draw(): Unit = sprite.draw()
}
