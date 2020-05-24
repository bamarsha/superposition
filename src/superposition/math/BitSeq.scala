package superposition.math

import scala.Function.const

class BitSeq private (private val s: Seq[Boolean]) {

  def apply(idx: Int): Boolean = s.lift(idx).getOrElse(false)

  val all: Boolean = s.forall(identity)

  val allNonEmpty: Boolean = s.forall(identity) && s.nonEmpty

  def and(b: BitSeq): BitSeq = new BitSeq(
    for {(x, y) <- s.zipAll(b.s, false, false)} yield x && y)

  val any: Boolean = s.exists(identity)

  val anyOrEmpty: Boolean = s.exists(identity) || s.isEmpty

  def extend(n: Int): BitSeq = new BitSeq(s ++ Seq.range(0, n).map(const(false)))

  def filter[A](o: Seq[A]): Seq[A] = o.zip(s).filter(_._2).map(_._1)

  val get: Seq[Boolean] = s

  def equals(s2: Seq[Boolean]): Boolean = s2 zip extend(s2.length).get forall { case (a, b) => a == b }

  def or(b: BitSeq): BitSeq = new BitSeq(
    for {(x, y) <- s.zipAll(b.s, false, false)} yield x || y)

  val toInt: Int = s.zipWithIndex.map({ case (a, b) => if (a) 1 << b else 0 }).sum

  def xor(b: BitSeq): BitSeq = new BitSeq(
    for {(x, y) <- s.zipAll(b.s, false, false)} yield x != y)

  override def equals(obj: Any): Boolean = obj match {
    case b: BitSeq => !xor(b).any
    case _ => false
  }
}

object BitSeq {

  def apply(bits: Boolean*): BitSeq = new BitSeq(bits)
}
