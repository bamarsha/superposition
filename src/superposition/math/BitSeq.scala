package superposition.math

import scala.collection.immutable.BitSet
import scala.sys.error

/** A sequence of bits.
  *
  * @param bits the bits in the sequence
  * @param length the length of the sequence
  */
final class BitSeq private(private val bits: BitSet, val length: Int) {
  /** Returns true if the `i`th bit is 1.
    *
    * @param i the index
    * @return true if the `i`th bit is 1
    */
  def apply(i: Int): Boolean = bits(i)

  /** True if any bit is 1. */
  def any: Boolean = bits.nonEmpty

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
  def filter[A](xs: Seq[A]): Seq[A] = (xs.view.zipWithIndex filter (x => bits(x._2)) map (_._1)).toSeq

  /** Converts the bit sequence to an integer.
    *
    * @throws RuntimeException if the value is too large
    */
  def toInt: Int = bits.toBitMask match {
    case Array(n) if n <= Int.MaxValue => n.toInt
    case _ => error("Bit sequence is too large")
  }

  override def equals(obj: Any): Boolean = obj match {
    case other: BitSeq => other.bits == bits && other.length == length
    case _ => false
  }

  override def hashCode: Int = bits.hashCode
}

/** Factories for bit sequences. */
object BitSeq {
  /** Converts a boolean sequence into a bit sequence.
    *
    * @param booleans the boolean sequence
    * @return the bit sequence
    */
  def apply(booleans: Boolean*): BitSeq =
    new BitSeq(BitSet((booleans.view.zipWithIndex filter (_._1) map (_._2)).toSeq: _*), booleans.length)

  /** The empty bit sequence. */
  val zero: BitSeq = BitSeq()
}
