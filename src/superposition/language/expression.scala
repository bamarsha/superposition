package superposition.language

/** An expression in a gate program. */
private sealed trait Expression

/** An identifier.
  *
  * @param name the name of the identifier
  */
private final case class Identifier(name: String) extends Expression

/** An integral number.
  *
  * @param value the value of the number
  */
private final case class IntegerNumber(value: Int) extends Expression

/** A floating-point number.
  *
  * @param value the value of the number
  */
private final case class DecimalNumber(value: Double) extends Expression

/** A tuple.
  *
  * @param items the items in the tuple
  */
private final case class Tuple(items: Seq[Expression]) extends Expression

/** A list.
  *
  * @param items the items in the list
  */
private final case class List(items: Seq[Expression]) extends Expression

/** A function call.
  *
  * @param function the function
  * @param argument the argument to the function
  */
private final case class Call(function: Expression, argument: Expression) extends Expression

/** An equals condition.
  *
  * @param lhs the left-hand side
  * @param rhs the right-hand side
  */
private final case class Equals(lhs: Expression, rhs: Expression) extends Expression
