package superposition

import scala.math.{atan2, cos, sin, sqrt}

private object Complex {
  def polar(r: Double, theta: Double) = Complex(r * cos(theta), r * sin(theta))
}

private case class Complex(real: Double, imag: Double = 0.0) {
  def +(that: Complex): Complex = Complex(real + that.real, imag + that.imag)

  def -(that: Complex): Complex = Complex(real - that.real, imag - that.imag)

  def *(that: Complex): Complex = Complex(
    real * that.real - imag * that.imag,
    real * that.imag + imag * that.real
  )

  def /(that: Complex): Complex = Complex(
    (real * that.real + imag * that.imag) / that.magnitudeSquared,
    (imag * that.real - real * that.imag) / that.magnitudeSquared
  )

  def magnitude: Double = sqrt(magnitudeSquared)

  def magnitudeSquared: Double = real * real + imag * imag

  def phase: Double = atan2(imag, real)
}
