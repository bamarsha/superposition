package superposition.entity

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.{Color, Texture}
import scalaz.syntax.monad._
import superposition.component._
import superposition.game.ResourceResolver.resolve
import superposition.math.{QExpr, StateId, Vector2}


/** A door blocks movement unless its control expression is true.
  *
  * @param multiverse the multiverse the door belongs to
  * @param cell the cell position of the door
  * @param size the size of the code for the lock
  * @param control the control for the door
  */
final class Lock(id: Int, multiverse: Multiverse, cell: Vector2[Int], size: Int, control: QExpr[Boolean]) extends Entity {
  val texture: Texture = new Texture(resolve("sprites/lock_" + size + ".png"))
  val bits: Seq[StateId[Boolean]] = Seq.tabulate(size)(i => multiverse.allocate("Bit " + i, false, if (_) "1" else "0"))

  add(new EntityId(id))
  add(new ClassicalPosition((cell map (_.toDouble)) + Vector2(0.5, 0.5)))
  add(new PrimaryBit(bits))
  add(new Renderable(1, control))
  add(new SpriteView(texture.pure[QExpr], color = control map (if (_) Color.GREEN else Color.WHITE)))
}
