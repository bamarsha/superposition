package superposition

/**
 * A quantum gate.
 */
private object Gate extends Enumeration {
  /**
   * The X, or NOT, logic gate.
   */
  val X: Value = Value

  /**
   * The Z logic gate.
   */
  val Z: Value = Value

  /**
   * The T logic gate.
   */
  val T: Value = Value

  /**
   * The Hadamard logic gate.
   */
  val H: Value = Value

  /**
   * The "move up" position gate.
   */
  val Up: Value = Value

  /**
   * The "move down" position gate.
   */
  val Down: Value = Value

  /**
   * The "move left" position gate.
   */
  val Left: Value = Value

  /**
   * The "move right" position gate.
   */
  val Right: Value = Value
}

/**
 * A condition that a universe must satisfy for a quantum gate to be applied.
 */
private sealed trait Control

/**
 * Controls a gate based on an object's bit.
 *
 * @param id the object's ID
 * @param on the necessary state of the object's bit
 */
private final case class BitControl(id: UniversalId, on: Boolean) extends Control

/**
 * Controls a gate based on an object's position.
 *
 * @param id   the object's ID
 * @param cell the necessary position of the object
 */
private final case class PositionControl(id: UniversalId, cell: Cell) extends Control
