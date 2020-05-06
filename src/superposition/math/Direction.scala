package superposition.math

import enumeratum._
import superposition.math.Direction.{Down, Left, Right, Up}

/** A cardinal direction. */
sealed trait Direction extends EnumEntry {
  /** The value of this direction as a [[superposition.math.Vector2i]]. */
  def toVector2i: Vector2i = this match {
    case Up => Vector2i(0, 1)
    case Down => Vector2i(0, -1)
    case Left => Vector2i(-1, 0)
    case Right => Vector2i(1, 0)
  }
}

/** The cardinal directions. */
object Direction extends Enum[Direction] {

  /** The up direction. */
  case object Up extends Direction

  /** The down direction. */
  case object Down extends Direction

  /** The left direction. */
  case object Left extends Direction

  /** The right direction. */
  case object Right extends Direction

  override def values: IndexedSeq[Direction] = findValues
}
