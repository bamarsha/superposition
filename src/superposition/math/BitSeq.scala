package superposition.math

/** A sequence of bits.
  *
  * @param bits the bits in the sequence
  * @param length the length of the sequence
  */
final class BitSeq private(private val bits: Int, val length: Int) {
  /** Returns true if the `i`th bit is 1.
    *
    * @param i the index
    * @return true if the `i`th bit is 1
    */
  def apply(i: Int): Boolean = ((bits >> i) & 1) == 1

  /** True if any bit is 1. */
  val any: Boolean = bits != 0

  /** Returns the bitwise AND of two bit sequences. The length of the new sequence is the length of the longer sequence.
    *
    * @param other the other bit sequence
    * @return the bitwise AND of the two bit sequences
    */
  def &(other: BitSeq): BitSeq = new BitSeq(bits & other.bits, length max other.length)

  /** Returns the bitwise OR of two bit sequences. The length of the new sequence is the length of the longer sequence.
    *
    * @param other the other bit sequence
    * @return the bitwise OR of the two bit sequences
    */
  def |(other: BitSeq): BitSeq = new BitSeq(bits | other.bits, length max other.length)

  /** Filters the sequence to contain only elements whose index corresponds to a 1 bit in the bit sequence.
    *
    * @param xs the sequence
    * @tparam A the element type
    * @return the filtered sequence
    */
  def filter[A](xs: Seq[A]): Seq[A] = (xs.view.zipWithIndex filter (x => this (x._2)) map (_._1)).toSeq

  /** Converts the bit sequence to an integer. */
  val toInt: Int = bits

  override def equals(obj: Any): Boolean = obj match {
    case other: BitSeq => other.bits == bits && other.length == length
    case _ => false
  }

  override val hashCode: Int = bits.hashCode
}

/** Factories for bit sequences. */
object BitSeq {
  /** Converts a boolean sequence into a bit sequence.
    *
    * @param booleans the boolean sequence
    * @return the bit sequence
    */
  def apply(booleans: Boolean*): BitSeq = new BitSeq(booleansToInt(booleans), booleans.length)

  /** Converts the boolean sequence to an integer.
    *
    * @param booleans the boolean sequence
    * @return the integer
    */
  private def booleansToInt(booleans: Seq[Boolean]): Int = {
    @scala.annotation.tailrec
    def toInt(index: Int, sum: Int): Int =
      if (index >= booleans.length) sum
      else toInt(index + 1, if (booleans(index)) sum + (1 << index) else sum)

    toInt(0, 0)
  }

  /** The empty bit sequence. */
  val zero: BitSeq = BitSeq()
}
