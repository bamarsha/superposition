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
  private val speed: Double = 5
}

/**
 * The player character in the game.
 */
private class Player extends Entity {
  import Player._

  /**
   * This player's physics component.
   */
  val physics: PhysicsComponent = require(classOf[PhysicsComponent])

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
    physics.velocity = new Vec2d(0, 0)
    if (Input.keyDown(GLFW_KEY_W)) {
      physics.velocity = physics.velocity.add(new Vec2d(0, speed))
    }
    if (Input.keyDown(GLFW_KEY_A)) {
      physics.velocity = physics.velocity.add(new Vec2d(-speed, 0))
    }
    if (Input.keyDown(GLFW_KEY_S)) {
      physics.velocity = physics.velocity.add(new Vec2d(0, -speed))
    }
    if (Input.keyDown(GLFW_KEY_D)) {
      physics.velocity = physics.velocity.add(new Vec2d(speed, 0))
    }

    if (Input.keyJustPressed(GLFW_KEY_SPACE)) {
      if (carrying.isEmpty) {
        carrying = Behavior
          .track(classOf[Quball])
          .asScala
          .filter(_.physics.position.sub(physics.position).length() < 0.5)
          .toList
      } else {
        carrying = List()
      }
    }
    carrying.foreach(quball => {
      quball.physics.position = physics.position
      quball.physics.velocity = physics.velocity
    })

    draw()
  }

  private def draw(): Unit =
    Sprite.load("cat.png").draw(Transformation.create(physics.position, 0, 1), WHITE)
}
