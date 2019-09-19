package superposition

/**
 * An object that can be drawn.
 */
private trait Drawable {
  /**
   * The layer to which this object belongs.
   */
  var layer: Int = 0

  /**
   * Draws this object.
   */
  def draw(): Unit
}
