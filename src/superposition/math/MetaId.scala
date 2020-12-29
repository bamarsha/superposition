package superposition.math

import superposition.math.QExpr.QExpr

/** An opaque identifier corresponding to metadata of a particular type.
  *
  * @tparam A the type of the metadata
  */
final class MetaId[A] extends DependentKey {

  /** The type of the metadata value. */
  type Value = A

  /** The value of the metadata. */
  val value: QExpr[A] = QExpr.ofMeta(this)
}
