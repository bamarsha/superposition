package superposition.math

import scala.collection.mutable

/** A mutable map in which the type of the value depends on the key.
  *
  * @tparam K the type of the key
  */
final class MutableDependentMap[K <: DependentKey] private (map: mutable.Map[K, Any])
    extends mutable.Cloneable[MutableDependentMap[K]] {

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
    */
  def update(key: K)(value: key.Value): Unit = map.update(key, value)

  /** Maps the value associated with the key to a new value.
    *
    * @param key the key
    * @param updater a function that maps the value of the key
    */
  def updateWith(key: K)(updater: key.Value => key.Value): Unit = update(key)(updater(this(key)))

  /** Removes the key.
    *
    * @param key the key to remove
    */
  def removed(key: K): Unit = map.remove(key)

  override def clone: MutableDependentMap[K] = new MutableDependentMap(map.clone)

  override def toString: String = map.toString
}

/** Factories for mutable dependent maps. */
object MutableDependentMap {

  /** A new empty mutable dependent map.
    *
    * @tparam K the type of the key.
    */
  def empty[K <: DependentKey]: MutableDependentMap[K] = new MutableDependentMap(new mutable.HashMap)
}
