package superposition.game.component

import com.badlogic.ashley.core._
import com.badlogic.gdx.Gdx.{gl, graphics, input}
import com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.{FrameBuffer, ShaderProgram}
import com.badlogic.gdx.math.Vector3
import scalaz.Scalaz._
import superposition.game.ResourceResolver.resolve
import superposition.game.component.Multiverse.combine
import superposition.math.{Complex, Vector2i}
import superposition.quantum.{Gate, MetaId, StateId, Universe}

import scala.Ordering.Implicits._
import scala.math.{Pi, sqrt}

final class Multiverse(val walls: Set[Vector2i], val camera: OrthographicCamera) extends Component {
  private var _universes: Seq[Universe] = Seq(Universe())

  private var _entities: List[Entity] = List()

  private var stateIds: List[StateId[_]] = List()

  private val shaderSettings: MetaId[(Float, Float)] = allocateMeta((0, 0))

  private var time: Float = 0

  private val universeShader: ShaderProgram = new ShaderProgram(
    resolve("shaders/universe.vert"),
    resolve("shaders/universe.frag"))

  private val universeBatch: SpriteBatch = new SpriteBatch(1000, universeShader)

  // TODO: Resize the framebuffer if the window is resized.
  private val frameBuffer: FrameBuffer = new FrameBuffer(RGBA8888, graphics.getWidth, graphics.getHeight, false)

  def universes: Seq[Universe] = _universes

  def entities: Iterable[Entity] = _entities

  def addEntity(entity: Entity): Unit = _entities ::= entity

  def allocate[A](initialValue: A): StateId[A] = {
    val id = new StateId[A]
    _universes = _universes map (_.updatedState(id)(initialValue))
    stateIds ::= id
    id
  }

  def allocateMeta[A](initialValue: A): MetaId[A] = {
    val id = new MetaId[A]
    _universes = _universes map (_.updatedMeta(id)(initialValue))
    id
  }

  def updateMetaWith(id: MetaId[_])(updater: id.Value => Universe => id.Value): Unit =
    _universes = _universes map (universe => universe.updatedMetaWith(id)(updater(_)(universe)))

  /**
   * Applies a gate to the multiverse.
   * <p>
   * If the gate produces any universe that is in an invalid state, no changes are made.
   *
   * @param gate  the gate to apply
   * @param value the value to give the gate
   * @return true if the gate was successfully applied
   */
  def applyGate[A](gate: Gate[A], value: A): Boolean = {
    val newUniverses = gate.applyToAll(value)(universes)
    if (newUniverses forall isValid) {
      _universes =
        (newUniverses
          |> combine
          |> (_.toSeq)
          |> (_ sortBy (universe => stateIds.reverse map (universe.state(_).toString))))
      true
    } else false
  }

  def allInCell(universe: Universe, cell: Vector2i): Iterable[Entity] =
    entities filter { entity =>
      QuantumPosition.Mapper.has(entity) && universe.state(QuantumPosition.Mapper.get(entity).cell) == cell
    }

  def toggles(universe: Universe, cell: Vector2i): Iterable[StateId[Boolean]] =
    allInCell(universe, cell) flatMap { entity =>
      if (Toggle.Mapper.has(entity)) Some(Toggle.Mapper.get(entity).toggle)
      else None
    }

  def isBlocked(universe: Universe, cell: Vector2i): Boolean =
    walls.contains(cell) ||
      (entities
        filter Collider.Mapper.has
        exists (Collider.Mapper.get(_).cells(universe).contains(cell)))

  def allOn(universe: Universe, controls: Iterable[Vector2i]): Boolean =
    controls forall { control =>
      entities exists { entity =>
        Activator.Mapper.has(entity) && QuantumPosition.Mapper.has(entity) &&
          universe.state(Activator.Mapper.get(entity).activator) &&
          universe.state(QuantumPosition.Mapper.get(entity).cell) == control
      }
    }

  def isValid(universe: Universe): Boolean =
    entities filter QuantumPosition.Mapper.has forall { entity =>
      !isBlocked(universe, universe.state(QuantumPosition.Mapper.get(entity).cell))
    }

  def isSelected(cell: Vector2i): Boolean = {
    val mouse = camera.unproject(new Vector3(input.getX, input.getY, 0))
    cell == Vector2i(mouse.x.floor.toInt, mouse.y.floor.toInt)
  }

  def updateShaderSettings(deltaTime: Float): Unit = {
    time += deltaTime
    var minValue: Float = 0
    _universes = for (universe <- universes) yield {
      val maxValue = minValue + universe.amplitude.squaredMagnitude.toFloat
      val newUniverse = universe.updatedMeta(shaderSettings)((minValue, maxValue))
      minValue = maxValue
      newUniverse
    }
  }

  def drawWithin(universe: Universe)(draw: => Unit): Unit = {
    frameBuffer.begin()
    gl.glClearColor(0, 0, 0, 0)
    gl.glClear(GL_COLOR_BUFFER_BIT)
    draw
    frameBuffer.end()
    val (minValue, maxValue) = universe.meta(shaderSettings)
    drawBuffer(universe, minValue, maxValue)
  }

  private def drawBuffer(universe: Universe, minValue: Float, maxValue: Float): Unit = {
    universeBatch.setProjectionMatrix(camera.combined)
    drawBufferWith {
      universeShader.setUniformf("time", time)
      universeShader.setUniformf("minVal", minValue)
      universeShader.setUniformf("maxVal", maxValue)
      universeShader.setUniformf("hue", (universe.amplitude.phase / (2 * Pi)).toFloat)
      universeShader.setUniform4fv("color", Array(1, 1, 1, 1), 0, 4)
    }
    drawBufferWith {
      universeShader.setUniformf("minVal", 0f)
      universeShader.setUniformf("maxVal", 1f)
      universeShader.setUniform4fv("color", Array(1, 1, 1, 0.1f), 0, 4)
    }
  }

  private def drawBufferWith(setup: => Unit): Unit = {
    universeBatch.begin()
    setup
    universeBatch.draw(
      frameBuffer.getColorBufferTexture, 0, camera.viewportHeight, camera.viewportWidth, -camera.viewportHeight)
    universeBatch.end()
  }

}

object Multiverse {
  val Mapper: ComponentMapper[Multiverse] = ComponentMapper.getFor(classOf[Multiverse])

  private def normalize(universes: Iterable[Universe]): Iterable[Universe] = {
    val sum = (universes map (_.amplitude.squaredMagnitude)).sum
    universes map (_ / Complex(sqrt(sum)))
  }

  private def combine(universes: Iterable[Universe]): Iterable[Universe] =
    (universes
      .groupMapReduce(_.state)(identity)(_ + _.amplitude)
      .values
      .filter(_.amplitude.squaredMagnitude > 1e-6)
      |> normalize)
}
