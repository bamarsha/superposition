package superposition.game

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color.{BLACK, WHITE}
import com.badlogic.gdx.graphics.Texture
import superposition.game.Cat.CatTexture
import superposition.game.ResourceResolver.resolve
import superposition.math.{Vector2d, Vector2i}

import scala.Function.const

/**
 * The player character in the game.
 *
 * @param multiverse  the multiverse this player belongs to
 * @param initialCell the initial position for this player
 */
private final class Cat(multiverse: Multiverse, initialCell: Vector2i) extends Entity {
  add(new Quantum(multiverse))

  locally {
    val alive = multiverse.allocate(true)
    val absolutePosition = multiverse.allocateMeta(initialCell.toVector2d + Vector2d(0.5, 0.5))
    val cell = multiverse.allocate(initialCell)

    add(new Player(alive))
    add(new Position(absolutePosition, cell, Vector2d(0.5, 0.5)))
    add(new BasicState(primaryBit = Some(alive), position = Some(cell)))
    add(new SpriteView(
      texture = const(CatTexture),
      position = _.meta(absolutePosition),
      scale = const(Vector2d(2, 2)),
      color = universe => if (universe.state(alive)) WHITE else BLACK))
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
}
