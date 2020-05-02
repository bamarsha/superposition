package superposition.game

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color.{BLACK, WHITE}
import com.badlogic.gdx.graphics.Texture
import superposition.game.Cat.CatTexture
import superposition.game.ResourceResolver.resolve
import superposition.math.{Vector2d, Vector2i}
import superposition.quantum.{MetaId, StateId}

import scala.Function.const

/**
 * The player character in the game.
 *
 * @param multiverse  the multiverse this player belongs to
 * @param initialCell the initial position for this player
 */
private final class Cat(multiverse: Multiverse, initialCell: Vector2i) extends Entity {
  add(new Player)
  add(new Quantum(multiverse))

  locally {
    val alive = multiverse.allocate(true)
    val position = multiverse.allocateMeta(initialCell.toVector2d + Vector2d(0.5, 0.5))
    val cell = multiverse.allocate(initialCell)

    add(new Position(position, cell, Vector2d(0.5, 0.5)))
    add(new BasicState(primaryBit = Some(alive), position = Some(cell)))
    add(new SpriteView(
      texture = const(CatTexture),
      position = _.meta(position),
      scale = const(Vector2d(2, 2)),
      universe => if (universe.state(alive)) WHITE else BLACK))
  }

//  private val walkGate: Gate[Vec2i] = {
//    val walkPlayer: Gate[Vec2i] = Translate.multi controlled { delta => universe =>
//      if (universe.state(alive)) List((cell, delta)) else List()
//    }
//    val walkQuballs: Gate[Vec2i] = Translate.multi controlled { delta => universe =>
//      if (universe.state(alive))
//        Quball.All
//          .filter(quball => universe.state(quball.carried))
//          .map(quball => (quball.cell, delta))
//          .toList
//      else List()
//    }
//    walkPlayer andThen walkQuballs
//  }
//
//  private val carryGate: Gate[Unit] =
//    X.multi controlled const { universe =>
//      Quball.All
//        .filter(quball => universe.state(alive) && universe.state(cell) == universe.state(quball.cell))
//        .map(_.carried)
//        .toList
//    }
//
//  private def walk(): Unit = {
//    def applyGate(delta: Vec2i) = multiverse.applyGate(walkGate, delta)
//
//    val Vec2i(dx, dy) = nextCell(relativePosition, deltaPosition)
//    val delta = deltaPosition add new Vec2d(
//      if (dx != 0 && applyGate(Vec2i(dx, 0))) -dx else 0,
//      if (dy != 0 && applyGate(Vec2i(0, dy))) -dy else 0)
//    relativePosition = (relativePosition add delta).clamp(0, 1)
//  }
//
//  private def updatePlayerPosition(): Unit =
//    multiverse.updateMetaWith(position) { pos => universe =>
//      if (universe.state(alive)) {
//        val targetPosition = universe.state(cell).toVec2d add relativePosition
//        pos.lerp(targetPosition, 10 * dt)
//      } else pos
//    }
//
//  private def updateCarriedPositions(): Unit =
//    for (quball <- Quball.All) {
//      multiverse.updateMetaWith(quball.position) { pos => universe =>
//        val relativePos = if (universe.state(quball.carried)) relativePosition else new Vec2d(0.5, 0.5)
//        val targetPos = universe.state(quball.cell).toVec2d add relativePos
//        if (universe.state(alive)) pos.lerp(targetPos, 10 * dt) else pos
//      }
//    }
//
//  private def step(): Unit = {
//    walk()
//    if (keyJustPressed(GLFW_KEY_SPACE)) {
//      multiverse.applyGate(carryGate, ())
//    }
//    updatePlayerPosition()
//    updateCarriedPositions()
//  }
}

private object Cat {
  private val CatTexture: Texture = new Texture(resolve("sprites/cat.png"))

//  def declareSystem(): Unit = Game.declareSystem(classOf[Player], (_: Player).step())
//
//  private def snapPosition(delta: Double): Int =
//    if (delta < -1e-3) -1
//    else if (delta > 1 + 1e-3) 1
//    else 0
//
//  private def nextCell(start: Vec2d, delta: Vec2d): Vec2i = {
//    val next = start add delta
//    Vec2i(snapPosition(next.x), snapPosition(next.y))
//  }
}
