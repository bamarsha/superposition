package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Vector3
import superposition.component.MultiverseView.UniversePartRenderer
import superposition.graphics.UniverseRenderInfo
import superposition.math.QExpr.QExpr
import superposition.math.Universe

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/** The multiverse view component manages the camera and provides rendering effects for the multiverse.
  *
  * @param multiverse the multiverse corresponding to this view
  * @param camera the camera used to view the multiverse
  */
final class MultiverseView(multiverse: Multiverse, val camera: Camera) extends Component {
  /** The queued renderers. */
  private val renderers: mutable.IndexedBuffer[UniversePartRenderer] = new ArrayBuffer

  /** Returns true if the cell is selected by the mouse.
    *
    * @param outline the outline to check
    * @return true if the cell is selected by the mouse
    */
  def isSelected(outline: Outline): Boolean = {
    val mouse = camera.unproject(new Vector3(input.getX.toFloat, input.getY.toFloat, 0))
    outline.lowerLeft.x <= mouse.x && mouse.x <= outline.lowerLeft.x + outline.size.x &&
      outline.lowerLeft.y <= mouse.y && mouse.y <= outline.lowerLeft.y + outline.size.y
  }

  /** Enqueues a renderer that will be called for each universe.
    *
    * @param renderable the renderable component for the rendered entity
    * @param render the rendering action
    */
  def enqueueRenderer(renderable: Renderable)(render: (Universe, UniverseRenderInfo) => Unit): Unit =
    renderers.append(UniversePartRenderer(render, renderable))

  /** Renders all of the queued renderers for the universe.
    *
    * Each renderer will be given the `renderInfo` only if its dependent state is not the same in all universes. If it
    * is the same, it will be given the default render information instead.
    *
    * @param universe the universe
    * @param renderInfo the rendering information for the universe
    */
  def render(universe: Universe, renderInfo: UniverseRenderInfo): Unit =
    for (renderer <- renderers.view sortBy (_.renderable.layer(universe))) {
      val dependentState = renderer.renderable.dependentState
      renderer.render(universe, if (isSameInAllUniverses(dependentState)) UniverseRenderInfo.default else renderInfo)
    }

  /** Clears the renderer queue for this frame. */
  def clearRenderers(): Unit = renderers.clear()

  /** Returns true if the dependent state is the same in all universes.
    *
    * @param dependentState the dependent state
    * @return true if the dependent state is the same in all universes
    */
  private def isSameInAllUniverses(dependentState: QExpr[Any]): Boolean = {
    val iterator = multiverse.universes.iterator
    var state = dependentState(iterator.next())
    var allSame = true
    while (allSame && iterator.hasNext) {
      val nextState = dependentState(iterator.next())
      allSame &&= state == nextState
      state = nextState
    }
    allSame
  }
}

/** Contains the component mapper for the multiverse view component. */
object MultiverseView {

  /** Renders a part of a universe.
    *
    * @param render the rendering action
    * @param renderable the renderable component for the rendered entity
    */
  private final case class UniversePartRenderer(
      render: (Universe, UniverseRenderInfo) => Unit,
      renderable: Renderable)

  /** The component mapper for the multiverse view component. */
  val mapper: ComponentMapper[MultiverseView] = ComponentMapper.getFor(classOf[MultiverseView])
}
