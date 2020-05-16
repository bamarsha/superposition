package superposition.component

import com.badlogic.ashley.core.{Component, Entity}

import scala.Function.const

/** A tag for the entity responsible for highlighting occupied cells in the multiverse. */
object CellHighlighter extends Component {
  /** Makes a renderable cell highlighter entity.
    *
    * @param layer the layer to render the cell highlights in
    * @return a renderable cell highlighter entity
    */
  def makeEntity(layer: Int): Entity = (new Entity)
    .add(new Renderable(layer, const(())))
    .add(CellHighlighter)
}
