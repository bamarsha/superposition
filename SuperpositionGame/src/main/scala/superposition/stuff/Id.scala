package superposition.stuff

import superposition.Complex

import scala.collection.immutable.Map
import scala.collection.immutable.TreeMap

trait Id {
  type T
}

trait Gate[T] {
  def forward(t: T)(u: Universe): List[Universe]
  def adjoint: Gate[T]
}

class IdMap private (m: Map[Id, Object]) {
  def get(key: Id): key.T = m.get(key).asInstanceOf[key.T]
  def set(key: Id)(value: key.T): IdMap = new IdMap(m + (key -> value.asInstanceOf[Object]))
}

object IdMap {
  val empty = new IdMap(new TreeMap[Id, Object]())
}

case class Universe(amplitude: Complex, state: IdMap) {
  def *(c: Complex): Universe = Universe(amplitude * c, state)
  def /(c: Complex): Universe = Universe(amplitude / c, state)
  def get(i: Id): i.T = state.get(i)
  def set(i: Id)(t: i.T): Universe = Universe(amplitude, state.set(i)(t))
}
