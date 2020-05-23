package superposition.math

class BitSeq(private val s: Seq[Boolean]) {

  def apply(idx: Int): Boolean = s.lift(idx).getOrElse(false)

  def all: Boolean = s.forall(identity)

  def allNonEmpty: Boolean = s.forall(identity) && s.nonEmpty

  def and(b: BitSeq): BitSeq = new BitSeq(
    for {(x, y) <- s.zipAll(b.s, false, false)} yield x && y)

  def any: Boolean = s.exists(identity)

  def anyOrEmpty: Boolean = s.exists(identity) || s.isEmpty

  def filter[A](o: Seq[A]): Seq[A] = o.zip(s).filter(_._2).map(_._1)

  def or(b: BitSeq): BitSeq = new BitSeq(
    for {(x, y) <- s.zipAll(b.s, false, false)} yield x || y)
}
