package superposition.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Vector3
import superposition.component.MultiverseView.UniversePartRenderer
import superposition.graphics.UniverseRenderInfo
import superposition.math.{QExpr, Universe, Vector2}

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
    * @param cell the cell to check
    * @return true if the cell is selected by the mouse
    */
  def isSelected(cell: Vector2[Int]): Boolean = {
    val mouse = camera.unproject(new Vector3(input.getX, input.getY, 0))
    cell == Vector2(mouse.x.floor.toInt, mouse.y.floor.toInt)
  }

  /** Enqueues a renderer that will be called for each universe.
    *
    * @param dependentState the value of the quantum state that the renderer depends on
    * @param render the rendering action
    */
  def enqueueRenderer(dependentState: QExpr[Any])(render: (Universe, UniverseRenderInfo) => Unit): Unit =
    renderers.append(UniversePartRenderer(render, dependentState))

  /** Renders all of the queued renderers for the universe.
    *
    * Each renderer will be given the `renderInfo` only if its dependent state is not the same in all universes. If it
    * is the same, it will be given the default render information instead.
    *
    * @param universe the universe
    * @param renderInfo the rendering information for the universe
    */
  def render(universe: Universe, renderInfo: UniverseRenderInfo): Unit =
    for (renderer <- renderers) {
      renderer.render(universe, if (isSameInAllUniverses(renderer)) UniverseRenderInfo.default else renderInfo)
    }

  /** Clears the renderer queue for this frame. */
  def clearRenderers(): Unit = renderers.clear()

  /** Returns true if the renderer's dependent state is the same in all universes.
    *
    * @param renderer the universe part renderer
    * @return true if the renderer's dependent state is the same in all universes
    */
  private def isSameInAllUniverses(renderer: UniversePartRenderer): Boolean = {
    val iterator = multiverse.universes.iterator
    var state = renderer.dependentState(iterator.next())
    var allSame = true
    while (allSame && iterator.hasNext) {
      val nextState = renderer.dependentState(iterator.next())
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
    * @param render the action that renders the part
    * @param dependentState the value of the quantum state that the renderer depends on
    */
  private final case class UniversePartRenderer(
      render: (Universe, UniverseRenderInfo) => Unit,
      dependentState: QExpr[Any])

  /** The component mapper for the multiverse view component. */
  val mapper: ComponentMapper[MultiverseView] = ComponentMapper.getFor(classOf[MultiverseView])
}
