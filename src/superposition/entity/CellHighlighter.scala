package superposition.entity

import cats.syntax.applicative.catsSyntaxApplicativeId
import com.badlogic.ashley.core.Entity
import superposition.component.{CellHighlightView, Renderable}
import superposition.math.QExpr.QExpr

/** A cell highlighter entity.
  *
  * @param layer the layer in which to render the cell highlighting
  */
final class CellHighlighter(layer: Int) extends Entity {
  add(new Renderable(layer, ().pure[QExpr]))
  add(CellHighlightView)
}
