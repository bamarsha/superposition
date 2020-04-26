package superposition.math

import scala.collection.immutable.HashMap

final class DependentMap[K <: DependentKey] private(private val map: HashMap[K, Any]) {
  def apply(key: K): key.Value = get(key).get

  def get(key: K): Option[key.Value] = map.get(key).asInstanceOf[Option[key.Value]]

  def updated(key: K)(value: key.Value): DependentMap[K] = new DependentMap(map.updated(key, value))

  def removed(key: K): DependentMap[K] = new DependentMap(map.removed(key))

  def updatedWith(key: K)(updater: Option[key.Value] => Option[key.Value]): DependentMap[K] =
    (get(key), updater(get(key))) match {
      case (None, None) => this
      case (Some(_), None) => removed(key)
      case (_, Some(value)) => updated(key)(value)
    }

  override def toString: String = map.toString

  override def equals(that: Any): Boolean = that match {
    case thatMap: DependentMap[_] => thatMap.map.equals(map)
    case _ => super.equals(that)
  }

  override def hashCode(): Int = map.hashCode()
}

object DependentMap {
  def empty[K <: DependentKey]: DependentMap[K] = new DependentMap(new HashMap)
}

trait DependentKey {
  type Value
}
