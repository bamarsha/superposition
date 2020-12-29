package superposition.entity

import cats.syntax.applicative.catsSyntaxApplicativeId
import com.badlogic.ashley.core.Entity
import superposition.component.{CellHighlightView, Renderable}
import superposition.math.QExpr.QExpr

/** A cell highlighter entity. */
object CellHighlighter {

  /** Creates a cell highlighter entity.
    *
    * @param layer the layer in which to render the cell highlighting
    */
  def apply(layer: Int): Entity = {
    val entity = new Entity
    entity.add(new Renderable(layer.pure[QExpr], ().pure[QExpr]))
    entity.add(CellHighlightView)
    entity
  }
}
