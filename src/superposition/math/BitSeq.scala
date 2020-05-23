package superposition.math

class BitSeq private (private val s: Seq[Boolean]) {

  def apply(idx: Int): Boolean = s.lift(idx).getOrElse(false)

  val all: Boolean = s.forall(identity)

  val allNonEmpty: Boolean = s.forall(identity) && s.nonEmpty

  def and(b: BitSeq): BitSeq = new BitSeq(
    for {(x, y) <- s.zipAll(b.s, false, false)} yield x && y)

  val any: Boolean = s.exists(identity)

  val anyOrEmpty: Boolean = s.exists(identity) || s.isEmpty

  def filter[A](o: Seq[A]): Seq[A] = o.zip(s).filter(_._2).map(_._1)

  val get: Seq[Boolean] = s

  def or(b: BitSeq): BitSeq = new BitSeq(
    for {(x, y) <- s.zipAll(b.s, false, false)} yield x || y)

  val toInt: Int = s.zipWithIndex.map({ case (a, b) => if (a) 1 << b else 0 }).sum
}

object BitSeq {

  def apply(bits: Boolean*): BitSeq = new BitSeq(bits)
}
