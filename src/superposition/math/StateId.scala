package superposition.math

/** An opaque identifier corresponding to a qudit of a particular type.
  *
  * @tparam A the type of the qudit
  */
final class StateId[A](val name: String, _printer: A => String) extends DependentKey {
  type Value = A
  val printer: Value => String = _printer
}
