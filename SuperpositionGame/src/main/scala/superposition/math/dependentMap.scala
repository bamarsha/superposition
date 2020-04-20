package superposition.math

import scala.collection.immutable.HashMap

final case class DependentMap[K <: DependentKey] private(map: HashMap[K, Any]) extends AnyVal {
  def apply(key: K): key.Value = get(key).get

  def get(key: K): Option[key.Value] = map.get(key).asInstanceOf[Option[key.Value]]

  def updated(key: K)(value: key.Value): DependentMap[K] = DependentMap(map.updated(key, value))

  def removed(key: K): DependentMap[K] = DependentMap(map.removed(key))

  def updatedWith(key: K)(updater: Option[key.Value] => Option[key.Value]): DependentMap[K] =
    (get(key), updater(get(key))) match {
      case (None, None) => this
      case (Some(_), None) => removed(key)
      case (_, Some(value)) => updated(key)(value)
    }
}

object DependentMap {
  def empty[K <: DependentKey]: DependentMap[K] = DependentMap(new HashMap)
}

trait DependentKey {
  type Value
}
