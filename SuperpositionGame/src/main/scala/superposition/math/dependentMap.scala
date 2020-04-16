package superposition.math

import scala.collection.immutable.HashMap

trait DependentKey {
  type Value
}

trait DependentMap[K <: DependentKey] extends DependentMapOps[K, DependentMap[K]]

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

final class DependentHashMap[K <: DependentKey] private(map: HashMap[K, Any])
  extends DependentMap[K]
    with DependentMapOps[K, DependentHashMap[K]] {
  override def get(key: K): Option[key.Value] =
    map.get(key).asInstanceOf[Option[key.Value]]

  override def updated(key: K)(value: key.Value): DependentHashMap[K] =
    new DependentHashMap(map.updated(key, value))

  override def removed(key: K): DependentHashMap[K] =
    new DependentHashMap(map.removed(key))
}

object DependentHashMap {
  def empty[K <: DependentKey]: DependentHashMap[K] = new DependentHashMap(new HashMap)
}
