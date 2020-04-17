package superposition.math

import scala.collection.immutable.HashMap

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
