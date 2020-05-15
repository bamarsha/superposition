package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Vector3
import superposition.game.UniverseRenderParams
import superposition.game.component.MultiverseView.UniversePartRenderer
import superposition.math.Vector2
import superposition.quantum.Universe

import scala.collection.immutable.Queue

/** The multiverse view component manages the camera and provides rendering effects for the multiverse.
  *
  * @param multiverse the multiverse corresponding to this view
  * @param camera the camera used to view the multiverse
  */
final class MultiverseView(multiverse: Multiverse, val camera: Camera) extends Component {
  /** The queued renderers. */
  private var renderers: Queue[UniversePartRenderer] = Queue.empty

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
    * The renderer will be given `Some(UniverseRenderParams)` only if the dependent state is not the same in all
    * universes. Otherwise, it will be given `None`.
    *
    * @param dependentState the value of the quantum state that the renderer depends on
    * @param render the rendering action
    */
  def enqueueRenderer(dependentState: Universe => Any)
                     (render: (Universe, Option[UniverseRenderParams]) => Unit): Unit =
    renderers = renderers enqueue UniversePartRenderer(render, dependentState)

  /** Renders all of the queued renderers for the universe.
    *
    * @param universe the universe
    * @param renderParams the rendering parameters for the universe
    */
  def render(universe: Universe, renderParams: UniverseRenderParams): Unit =
    for (renderer <- renderers) {
      val isSameInAllUniverses = (multiverse.universes map renderer.dependentState).toSet.size == 1
      renderer.render(universe, if (isSameInAllUniverses) None else Some(renderParams))
    }

  /** Clears the renderer queue for this frame. */
  def clearRenderers(): Unit = renderers = Queue.empty
}

/** Contains the component mapper for the multiverse view component. */
object MultiverseView {

  /** Renders a part of a universe.
    *
    * @param render the action that renders the part
    * @param dependentState the value of the quantum state that the renderer depends on
    */
  private final case class UniversePartRenderer(
      render: (Universe, Option[UniverseRenderParams]) => Unit,
      dependentState: Universe => Any)

  /** The component mapper for the multiverse view component. */
  val Mapper: ComponentMapper[MultiverseView] = ComponentMapper.getFor(classOf[MultiverseView])
}
