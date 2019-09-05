package superposition

import engine.core.Behavior.Entity
import engine.core.Game
import engine.graphics.sprites.Sprite
import engine.util.Color.WHITE
import engine.util.math.Vec2d
import extras.physics.PositionComponent

/**
 * Contains initialization for machines.
 */
private object Machine {
  /**
   * Declares the machine system.
   */
  def declareSystem(): Unit =
    Game.declareSystem(classOf[Machine], (_: Machine).step())
}

/**
 * A machine applies a CNOT gate to the target qubit. It is activated when a qubit comes near the machine, which becomes
 * the control qubit.
 *
 * @param multiverse the multiverse this machine belongs to
 * @param target     the target qubit
 * @param _position  the position of this machine
 */
private final class Machine(multiverse: Multiverse, target: UniversalId, _position: Vec2d) extends Entity {
  private val position: PositionComponent = add(new PositionComponent(this, _position))

  add(new Drawable(this, Sprite.load(getClass.getResource("sprites/tile1.png")), color = WHITE))
  add(new Draw(this))

  private var control: Option[UniversalId] = None

  private def step(): Unit = {
    val near = multiverse.qubitsNear(position.value)
    val last = control
    control = control match {
      case Some(id) => if (near.contains(id)) Some(id) else near.headOption
      case None => near.headOption
    }
    if (control != last) {
      if (last.isDefined) {
        // Undo the last controlled gate.
        multiverse.applyGate(Gate.X, target, last.get)
      }
      if (control.isDefined) {
        // Apply the new controlled gate.
        multiverse.applyGate(Gate.X, target, control.get)
      }
    }
  }
}
