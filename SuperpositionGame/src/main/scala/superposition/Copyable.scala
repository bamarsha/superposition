package superposition

/**
 * An object that can be copied.
 *
 * @tparam A the type of the copy
 */
private trait Copyable[A] {
  /**
   * Copies this object.
   *
   * @return a copy of this object
   */
  def copy(): A
}
