package superposition.types.quantum

import scala.collection.immutable.{Map, TreeMap}

trait Id[T2] {
  type T = T2
}

class IdMap private (m: Map[Id[_], Object]) {
  def get(key: Id[_]): key.T = m.get(key).asInstanceOf[key.T]
  def set(key: Id[_])(value: key.T): IdMap = new IdMap(m + (key -> value.asInstanceOf[Object]))
}

object IdMap {
  val empty = new IdMap(new TreeMap[Id[_], Object]())
}
