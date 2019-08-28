package superposition

import engine.core.Behavior.Entity
import engine.core.{Behavior, Input}
import engine.graphics.sprites.Sprite
import engine.util.Color.WHITE
import engine.util.math.{Transformation, Vec2d}
import extras.physics.{PhysicsComponent, Rectangle}
import org.lwjgl.glfw.GLFW.{GLFW_KEY_A, GLFW_KEY_D, GLFW_KEY_S, GLFW_KEY_SPACE, GLFW_KEY_W}

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
private class Player(position: Vec2d, universe: Universe) extends Entity {
  import Player._

  /**
   * This player's physics component.
   */
  val physics: PhysicsComponent = require(classOf[PhysicsComponent])
  physics.position = position

  val gameObject: GameObject = require(classOf[GameObject])
  gameObject.universe = universe
  gameObject.copy = universe => new Player(physics.position, universe).create()
  gameObject.draw = () =>
    PlayerSprite.draw(Transformation.create(physics.position, 0, 1), WHITE)

  private var carrying: List[Quball] = List()

  override protected def onCreate(): Unit =
    physics.collider = PhysicsComponent.wallCollider(new Vec2d(1, 1), List(
      new Rectangle(new Vec2d(-8, -4.5), new Vec2d(-8, 4.5)),
      new Rectangle(new Vec2d(-8, -4.5), new Vec2d(8, -4.5)),
      new Rectangle(new Vec2d(-8, 4.5), new Vec2d(8, 4.5)),
      new Rectangle(new Vec2d(8, -4.5), new Vec2d(8, 4.5))
    ).asJavaCollection)

  /**
   * Steps time forward for this player.
   */
  def step(): Unit = {
    physics.velocity = walkVelocity()
    if (Input.keyJustPressed(GLFW_KEY_SPACE)) {
      toggleCarrying()
    }
    for (quball <- carrying) {
      quball.physics.position = physics.position
      quball.physics.velocity = physics.velocity
    }
  }

  private def toggleCarrying(): Unit = {
    if (carrying.isEmpty) {
      val nearby = Behavior
        .track(classOf[Quball])
        .asScala
        .filter(_.physics.position.sub(physics.position).length() < 0.5)
        .groupBy(_.qubit)
      carrying = if (nearby.isEmpty) List() else nearby.head._2.toList
    } else {
      carrying = List()
    }
  }
}
