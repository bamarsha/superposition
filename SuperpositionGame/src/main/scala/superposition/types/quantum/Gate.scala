package superposition.types.quantum

import superposition.types.math.{Cell, Complex}

import scala.math.sqrt

trait Gate[T] {
  def forward(t: T)(u: Universe): List[Universe]
  def adjoint: Gate[T]

  def map[S](f: S => T): Gate[S] = new Gate[S] {
    override def forward(s: S)(u: Universe): List[Universe] = Gate.this.forward(f(s))(u)
    override def adjoint: Gate[S] = Gate.this.adjoint.map(f)
  }
}

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
    override def adjoint: Gate[List[T]] = multi(gate.adjoint).map((_: List[T]).reverse)
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

  def X: Gate[Id[Boolean]] = new Gate[Id[Boolean]] {
    override def forward(t: Id[Boolean])(u: Universe): List[Universe] = List(u.set(t)(!u.get(t)))
    override def adjoint: Gate[Id[Boolean]] = X
  }

  def H: Gate[Id[Boolean]] = new Gate[Id[Boolean]] {
    override def forward(t: Id[Boolean])(u: Universe): List[Universe] =
      List(u / Complex(sqrt(2) * (if (u.get(t)) -1 else 1)), u.set(t)(!u.get(t)) / Complex(sqrt(2)))
    override def adjoint: Gate[Id[Boolean]] = H
  }

  def translate: Gate[(Id[Cell], Int, Int)] = new Gate[(Id[Cell], Int, Int)] {
    override def forward(t: (Id[Cell], Int, Int))(u: Universe): List[Universe] = t match {
      case (i, x, y) => List(u.set(i)(u.get(i).translate(x, y)))
    }
    override def adjoint: Gate[(Id[Cell], Int, Int)] =
      translate.map { case (i: Id[Cell], x: Int, y: Int) => (i, -x, -y) }
  }

  // Helper functions below

  def flatmap[S, T](f: S => List[T]): Gate[T] => Gate[S] = multi(_).map(f)

  def filter[T](pred: T => Boolean): Gate[T] => Gate[T] = flatmap(if (pred(_)) List(_) else List())

}
