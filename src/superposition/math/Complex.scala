package superposition.math

import scala.math.{atan2, cos, sin, sqrt}

/** A complex number.
  *
  * @param real the real part
  * @param imaginary the imaginary part
  */
final case class Complex(real: Double, imaginary: Double = 0) {
  /** Adds two complex numbers.
    *
    * @param that the complex number to add to this
    * @return the sum of the two complex numbers
    */
  def +(that: Complex): Complex = Complex(real + that.real, imaginary + that.imaginary)

  /** Subtracts two complex numbers.
    *
    * @param that the complex number to subtract from this
    * @return the difference of the two complex numbers
    */
  def -(that: Complex): Complex = Complex(real - that.real, imaginary - that.imaginary)

  /** Multiplies two complex numbers.
    *
    * @param that the complex number to multiply with this
    * @return the product of the two complex numbers
    */
  def *(that: Complex): Complex = Complex(
    real * that.real - imaginary * that.imaginary,
    real * that.imaginary + imaginary * that.real)

  /** Divides two complex numbers.
    *
    * @param that the complex number to divide into this
    * @return the quotient of the two complex numbers
    */
  def /(that: Complex): Complex = Complex(
    (real * that.real + imaginary * that.imaginary) / that.squaredMagnitude,
    (imaginary * that.real - real * that.imaginary) / that.squaredMagnitude)

  /** The magnitude, or absolute value, of this complex number. */
  def magnitude: Double = sqrt(squaredMagnitude)

  /** The squared magnitude, or squared absolute value, of this complex number. */
  def squaredMagnitude: Double = real * real + imaginary * imaginary

  /** The phase angle θ when this complex number is written in the polar form re^iθ^. */
  def phase: Double = atan2(imaginary, real)

  /** The complex conjugate of this number. */
  def conjugate: Complex = Complex(real, -imaginary)
}

/** Complex number factory. */
object Complex {
  /** Creates a complex number from its polar form re^iθ^.
    *
    * @param r the radius r
    * @param theta the angle θ
    * @return the complex number re^iθ^
    */
  def polar(r: Double, theta: Double): Complex = Complex(r * cos(theta), r * sin(theta))
}
