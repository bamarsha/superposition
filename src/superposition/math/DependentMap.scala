package superposition.math

import scala.collection.immutable.HashMap

/** A map in which the type of the value depends on the key.
  *
  * @tparam K the type of the key
  */
final class DependentMap[K <: DependentKey] private(private val map: HashMap[K, Any]) extends AnyVal {
  /** Returns the value associated with the key.
    *
    * @param key the key
    * @throws NoSuchElementException if the key does not exist
    * @return the value associated with the key
    */
  def apply(key: K): key.Value = map(key).asInstanceOf[key.Value]

  /** Optionally returns the value associated with the key.
    *
    * @param key the key
    * @return the value associated with the key or `None` if none exists
    */
  def get(key: K): Option[key.Value] = map.get(key).asInstanceOf[Option[key.Value]]

  /** Adds or replaces the value associated with the key.
    *
    * @param key the key
    * @param value the new value associated with the key
    * @return the updated map
    */
  def updated(key: K)(value: key.Value): DependentMap[K] = new DependentMap(map.updated(key, value))

  /** Maps the optional value associated with the key to a new optional value.
    *
    * @param key the key
    * @param updater a function that maps the value of the key
    * @return the updated map
    */
  def updatedWith(key: K)(updater: Option[key.Value] => Option[key.Value]): DependentMap[K] =
    (get(key), updater(get(key))) match {
      case (None, None) => this
      case (Some(_), None) => removed(key)
      case (_, Some(value)) => updated(key)(value)
    }

  /** Removes the key.
    *
    * @param key the key to remove
    * @return the updated map
    */
  def removed(key: K): DependentMap[K] = new DependentMap(map.removed(key))

  override def toString: String = map.toString
}

/** Factories for dependent maps. */
object DependentMap {
  /** An empty map.
    *
    * @tparam K the type of the key.
    */
  def empty[K <: DependentKey]: DependentMap[K] = new DependentMap(new HashMap)
}
