package superposition.math

/** An opaque identifier corresponding to a qudit of a particular type.
  *
  * @param name the name of the qudit
  * @param showValue a function that maps qudit values to strings
  * @tparam A the type of the qudit
  */
final class StateId[A](val name: String, showValue: A => String) extends DependentKey {
  /** The type of the qudit value. */
  type Value = A

  /** Maps a value of the qudit to a string.
    *
    * @param value the value
    * @return the value as a string
    */
  def show(value: Value): String = showValue(value)

  /** The value of the qudit. */
  val value: QExpr[A] = QExpr(this)
}
