package superposition

import engine.core.Behavior.Entity
import engine.core.Game.dt
import engine.core.Input
import org.lwjgl.glfw.GLFW._

import scala.math.{Pi, sqrt}

private object Multiverse {
  private object Gate extends Enumeration {
    val X, Z, T, H = Value
  }

  private val GateKeys = List(
    (GLFW_KEY_X, Gate.X),
    (GLFW_KEY_Z, Gate.Z),
    (GLFW_KEY_T, Gate.T),
    (GLFW_KEY_H, Gate.H)
  )

  private val NumObjects: Int = 2
}

private class Multiverse extends Entity {
  import Multiverse._

  private var universes: List[Universe] = List(new Universe(NumObjects))
  private var time: Double = 0

  private def applyGate(gate: Gate.Value, target: Int, controls: Int*): Unit = {
    for (u <- universes.filter(u => controls.forall(u.bits(_).on))) {
      gate match {
        case Gate.X => u.bits(target).on = !u.bits(target).on
        case Gate.Z =>
          if (u.bits(target).on) {
            u.amplitude *= Complex(-1)
          }
        case Gate.T =>
          if (u.bits(target).on) {
            u.amplitude *= Complex.polar(1, Pi / 4)
          }
        case Gate.H =>
          u.amplitude /= Complex(sqrt(2))
          val copy = u.copy()
          if (u.bits(target).on) {
            u.amplitude *= Complex(-1)
          }
          copy.bits(target).on = !copy.bits(target).on
          universes = copy :: universes
      }
    }
  }

  private def combine(): Unit =
    universes = universes
      .groupMapReduce(_.state)(identity)((u1, u2) => {
        u2.amplitude += u1.amplitude
        u2
      })
      .values
      .filter(_.amplitude.squaredMagnitude > 1e-6)
      .toList

  private def normalize(): Unit = {
    val sum = universes.map(_.amplitude.squaredMagnitude).sum
    for (u <- universes) {
      u.amplitude /= Complex(sqrt(sum))
    }
  }

  def step(): Unit = {
    val selected = universes
      .flatMap(_.bits.zipWithIndex)
      .filter { case (bit, _) => bit.position.sub(Input.mouse()).length() < 0.5 }
      .map { case (_, index) => index }
      .toSet
    for ((key, gate) <- GateKeys) {
      if (Input.keyJustPressed(key)) {
        selected.foreach(applyGate(gate, _))
      }
    }
    universes.foreach(_.step())
    combine()
    normalize()
    draw()
  }

  private def draw(): Unit = {
    time += dt()
    var minValue = 0.0
    for (u <- universes) {
      val maxValue = minValue + u.amplitude.squaredMagnitude
      u.draw(time, minValue, maxValue)
      minValue = maxValue
    }
  }
}
