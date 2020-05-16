package superposition.game.component

import com.badlogic.ashley.core.{Component, Entity}

import scala.Function.const

/** A tag for the entity responsible for highlighting occupied cells in the multiverse. */
final class CellHighlighter extends Component

/** Contains the factory for the cell highlighter. */
object CellHighlighter {
  /** Makes a renderable cell highlighter entity.
    *
    * @param layer the layer to render the cell highlights in
    * @return a renderable cell highlighter entity
    */
  def makeEntity(layer: Int): Entity = (new Entity)
    .add(new Renderable(layer, const(())))
    .add(new CellHighlighter)
}
