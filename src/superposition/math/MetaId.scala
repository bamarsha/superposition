package superposition.math

/** An opaque identifier corresponding to metadata of a particular type.
  *
  * @tparam A the type of the metadata
  */
final class MetaId[A] extends DependentKey {
  type Value = A
}
