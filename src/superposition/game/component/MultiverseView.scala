package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Vector3
import superposition.math.Vector2
import superposition.quantum.Universe

import scala.collection.immutable.Queue

/** The multiverse view component manages the camera and provides rendering effects for the multiverse.
  *
  * @param multiverse the multiverse corresponding to this view
  * @param camera the camera used to view the multiverse
  */
final class MultiverseView(multiverse: Multiverse, val camera: Camera) extends Component {
  /** The queued drawing actions. */
  private var drawings: Queue[Universe => Unit] = Queue.empty

  /** Returns true if the cell is selected by the mouse.
    *
    * @param cell the cell to check
    * @return true if the cell is selected by the mouse
    */
  def isSelected(cell: Vector2[Int]): Boolean = {
    val mouse = camera.unproject(new Vector3(input.getX, input.getY, 0))
    cell == Vector2(mouse.x.floor.toInt, mouse.y.floor.toInt)
  }

  /** Enqueues a drawing action that will be performed for each universe.
    *
    * @param drawing the drawing action
    */
  def enqueueDrawing(drawing: Universe => Unit): Unit = drawings = drawings enqueue drawing

  /** Draws all of the queued drawing actions for the universe.
    *
    * @param universe the universe
    */
  def drawAll(universe: Universe): Unit = drawings.foreach(_.apply(universe))

  /** Empties the drawing action queue. */
  def emptyDrawingQueue(): Unit = drawings = Queue.empty
}

/** Contains the component mapper for the multiverse view component. */
object MultiverseView {
  /** The component mapper for the multiverse view component. */
  val Mapper: ComponentMapper[MultiverseView] = ComponentMapper.getFor(classOf[MultiverseView])
}
