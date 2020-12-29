package superposition.math

import enumeratum._
import superposition.math.Direction.{Down, Left, Right, Up}

/** A cardinal direction. */
sealed trait Direction extends EnumEntry {

  /** The value of this direction as a [[superposition.math.Vector2]]. */
  def toVector2: Vector2[Int] = this match {
    case Up => Vector2(0, 1)
    case Down => Vector2(0, -1)
    case Left => Vector2(-1, 0)
    case Right => Vector2(1, 0)
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
