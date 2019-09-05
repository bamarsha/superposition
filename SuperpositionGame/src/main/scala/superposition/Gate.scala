package superposition

/**
 * A quantum logic gate.
 */
private object Gate extends Enumeration {
  /**
   * The X, or NOT, gate.
   */
  val X: Gate.Value = Value

  /**
   * The Z gate.
   */
  val Z: Gate.Value = Value

  /**
   * The T gate.
   */
  val T: Gate.Value = Value

  /**
   * The Hadamard gate.
   */
  val H: Gate.Value = Value
}
