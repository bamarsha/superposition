package superposition.types.math

/**
 * A cell in a grid.
 *
 * @param x    the x position of this cell
 * @param y    the y position of this cell
 */
final case class Cell(x: Int, y: Int) {
  def translate(x2: Int, y2: Int): Cell = Cell(x + x2, y + y2)
}
