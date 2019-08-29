package superposition

import engine.core.Behavior.Entity
import engine.core.Input
import engine.graphics.sprites.Sprite
import engine.util.Color.WHITE
import engine.util.math.{Transformation, Vec2d}
import extras.physics.{PhysicsComponent, Rectangle}
import org.lwjgl.glfw.GLFW._

import scala.jdk.CollectionConverters._

private object Player {
  private val Speed: Double = 5

  private val WalkKeys: List[(Int, Vec2d)] = List(
    (GLFW_KEY_W, new Vec2d(0, 1)),
    (GLFW_KEY_A, new Vec2d(-1, 0)),
    (GLFW_KEY_S, new Vec2d(0, -1)),
    (GLFW_KEY_D, new Vec2d(1, 0))
  )

  private val PlayerSprite: Sprite = Sprite.load(getClass.getResource("sprites/cat.png"))

  private def walkVelocity(): Vec2d = {
    val direction = WalkKeys.foldLeft(new Vec2d(0, 0)) {
      case (acc, (key, direction)) => if (Input.keyDown(key)) acc.add(direction) else acc
    }
    if (direction.length() == 0) direction else direction.setLength(Speed)
  }
}

/**
 * The player character in the game.
 */
private class Player(universe: Universe, position: Vec2d) extends Entity {
  import Player._

  private val physics: PhysicsComponent = require(classOf[PhysicsComponent])
  physics.position = position
  physics.collider = PhysicsComponent.wallCollider(
    new Vec2d(1, 1),
    List(
      new Rectangle(new Vec2d(-8, -4.5), new Vec2d(-8, 4.5)),
      new Rectangle(new Vec2d(-8, -4.5), new Vec2d(8, -4.5)),
      new Rectangle(new Vec2d(-8, 4.5), new Vec2d(8, 4.5)),
      new Rectangle(new Vec2d(8, -4.5), new Vec2d(8, 4.5))
    ).asJavaCollection
  )

  private val gameObject: GameObject = require(classOf[GameObject])
  gameObject.universe = universe
  gameObject.copyTo = copyTo
  gameObject.draw = draw

  private var carrying: Option[GameObject] = None

  /**
   * Steps time forward for this player.
   */
  def step(): Unit = {
    physics.velocity = walkVelocity()
    if (Input.keyJustPressed(GLFW_KEY_SPACE)) {
      toggleCarrying()
    }
    for (gameObject <- carrying) {
      gameObject.physics.position = physics.position
      gameObject.physics.velocity = physics.velocity
    }
  }

  private def toggleCarrying(): Unit =
    if (carrying.isEmpty)
      carrying = gameObject.universe.gameObjects.find(
        o => o.entity != this && o.physics.position.sub(physics.position).length() < 0.5
      )
    else
      carrying = None

  private def copyTo(universe: Universe): Unit =
    new Player(universe, physics.position).create()

  private def draw(): Unit =
    PlayerSprite.draw(Transformation.create(physics.position, 0, 1), WHITE)
}
