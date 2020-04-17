package superposition.math

trait DependentMapOps[K <: DependentKey, +Self <: DependentMapOps[K, Self]] {
  this: Self =>

  def get(key: K): Option[key.Value]

  def updated(key: K)(value: key.Value): Self

  def removed(key: K): Self

  def apply(key: K): key.Value = get(key).get

  def updatedWith(key: K)(updater: Option[key.Value] => Option[key.Value]): Self =
    (get(key), updater(get(key))) match {
      case (None, None) => this
      case (Some(_), None) => removed(key)
      case (_, Some(value)) => updated(key)(value)
    }
}
