package superposition.entity

import com.badlogic.ashley.core.Entity
import superposition.component.{CellHighlightView, Renderable}

import scala.Function.const

/** A cell highlighter entity.
  *
  * @param layer the layer in which to render the cell highlighting
  */
final class CellHighlighter(layer: Int) extends Entity {
  add(new Renderable(layer, const(())))
  add(CellHighlightView)
}
