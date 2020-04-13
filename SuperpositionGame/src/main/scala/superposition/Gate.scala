package superposition

import scala.math.sqrt

object Gate {

  def compose[T](gates: List[Gate[T]]): Gate[T] = new Gate[T] {
    override def forward(t: T)(u: Universe): List[Universe] = gates match {
      case List() => List(u)
      case gate :: rest => gate.forward(t)(u).flatMap(compose(rest).forward(t))
    }
    override def adjoint: Gate[T] = compose(gates.reverse.map(_.adjoint))
  }

  def multi[T](gate: Gate[T]): Gate[List[T]] = new Gate[List[T]] {
    override def forward(ts: List[T])(u: Universe): List[Universe] = ts match {
      case List() => List(u)
      case t :: rest => gate.forward(t)(u).flatMap(multi(gate).forward(rest))
    }
    override def adjoint: Gate[List[T]] = map((_: List[T]).reverse)(multi(gate.adjoint))
  }

  def map[S, T](f: S => T)(gate: Gate[T]): Gate[S] = new Gate[S] {
    override def forward(s: S)(u: Universe): List[Universe] = gate.forward(f(s))(u)
    override def adjoint: Gate[S] = map(f)(gate.adjoint)
  }

  def control[S, T](f: S => Universe => T)(gate: Gate[T]): Gate[S] = new Gate[S] {
    override def forward(s: S)(u: Universe): List[Universe] = {
      val output = gate.forward(f(s)(u))(u)
      for (u2 <- output) require(f(s)(u) == f(s)(u2))
      output
    }
    override def adjoint: Gate[S] = control(f)(gate.adjoint)
  }

  def applyToAll[T](t: T)(gate: Gate[T])(us: List[Universe]): List[Universe] = us.flatMap(gate.forward(t))

  def X: Gate[ObjectId] = new Gate[ObjectId] {
    override def forward(t: ObjectId)(u: Universe): List[Universe] = {
      val k = u.bitMaps(t).defaultKey
      val newU = u.copy()
      newU.bitMaps(t).state += k -> !newU.bitMaps(t).state(k)
      List(newU)
    }
    override def adjoint: Gate[ObjectId] = X
  }

  def H: Gate[ObjectId] = new Gate[ObjectId] {
    override def forward(t: ObjectId)(u: Universe): List[Universe] = {
      val k = u.bitMaps(t).defaultKey
      val newU = u.copy()
      newU.amplitude /= Complex(sqrt(2) * (if (u.bitMaps(t).state(k)) -1 else 1))
      val newU2 = u.copy()
      newU2.amplitude /= Complex(sqrt(2))
      newU2.bitMaps(t).state += k -> !newU2.bitMaps(t).state(k)
      List(newU, newU2)
    }
    override def adjoint: Gate[ObjectId] = H
  }

  def translate: Gate[(ObjectId, Int, Int)] = new Gate[(ObjectId, Int, Int)] {
    override def forward(t: (ObjectId, Int, Int))(u: Universe): List[Universe] = {
      val (o, x, y) = t

    }
  }

  // Helper functions below

  def flatmap[S, T](f: S => List[T]): Gate[T] => Gate[S] = map(f) compose multi

  def filter[T](pred: T => Boolean): Gate[T] => Gate[T] = flatmap(if (pred(_)) List(_) else List())

}
