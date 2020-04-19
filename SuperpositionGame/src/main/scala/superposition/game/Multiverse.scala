package superposition.game

import engine.core.Behavior.Entity
import engine.core.Game
import engine.core.Game.dt
import engine.graphics.Camera
import engine.graphics.Camera.Camera2d
import engine.graphics.Graphics.drawRectangle
import engine.graphics.opengl.{Framebuffer, Shader, Texture}
import engine.util.Color
import engine.util.Color.CLEAR
import engine.util.math.{Transformation, Vec2d, Vec4d}
import extras.physics.Rectangle
import extras.tiles.{Tilemap, TilemapRenderer}
import scalaz.Scalaz._
import superposition.game.Multiverse.{UniverseShader, combine, walls}
import superposition.game.UniverseImplicits.GameUniverse
import superposition.math.{Complex, Vec2i}
import superposition.quantum.{Gate, MetaId, StateId, Universe}

import scala.Function.const
import scala.Ordering.Implicits._
import scala.jdk.CollectionConverters._
import scala.math.{Pi, sqrt}

/**
 * The multiverse is a collection of universes.
 *
 * Multiple universes represent qubits in superposition. The multiverse can apply quantum gates to qubits by changing
 * the amplitude of a universe or creating a copy of a universe.
 *
 * @param tileMap the tile map for the multiverse
 */
private final class Multiverse(tileMap: Tilemap) extends Entity {
  /**
   * The bounding box of the multiverse's tile map.
   */
  val boundingBox: Rectangle = new Rectangle(new Vec2d(0, 0), new Vec2d(tileMap.width, tileMap.height))

  private var universes: List[Universe] = List(Universe())

  private var entities: List[Entity] = List()

  private var stateIds: List[StateId[_]] = List()

  private var time: Double = 0

  private val tileRenderer: TilemapRenderer =
    new TilemapRenderer(tileMap, source => Texture.load(getClass.getResource(source)))

  private var frameBuffer: Framebuffer = _

  private var colorBuffer: Texture = _

  add(new UniverseComponent(this, blockingCells = const(walls(tileMap))))

  override protected def onCreate(): Unit = {
    frameBuffer = new Framebuffer()
    colorBuffer = frameBuffer.attachColorBuffer()
  }

  override protected def onDestroy(): Unit = entities.foreach(Game.destroy)

  /**
   * Applies a gate to the multiverse.
   *
   * If the gate produces any universe that is in an invalid state, no changes are made.
   *
   * @param gate  the gate to apply
   * @param value the value to give the gate
   * @return whether the gate was successfully applied
   */
  def applyGate[A](gate: Gate[A], value: A): Boolean = {
    val newUniverses = gate.applyToAll(value)(universes)
    if (newUniverses forall (_.isValid)) {
      universes = newUniverses |>
        combine |>
        (_ sortBy (universe => stateIds.reverse map (universe.state(_).toString)))
      true
    } else false
  }

  def allocate[A](initialValue: A): StateId[A] = {
    val id = new StateId[A]
    universes = universes map (_.updatedState(id)(initialValue))
    stateIds ::= id
    id
  }

  def allocateMeta[A](initialValue: A): MetaId[A] = {
    val id = new MetaId[A]
    universes = universes map (_.updatedMeta(id)(initialValue))
    id
  }

  def addEntity(entity: Entity): Unit = {
    Game.create(entity)
    entities ::= entity
  }

  def forall(f: Universe => Boolean): Boolean = universes forall f

  def updateMetaWith(id: MetaId[_])(updater: id.Value => Universe => id.Value): Unit =
    universes = universes map (universe => universe.updatedMetaWith(id)(updater(_)(universe)))

  private def step(): Unit = draw()

  private def draw(): Unit = {
    tileRenderer.draw(Transformation.IDENTITY, Color.WHITE)
    highlightOccupiedCells()
    drawShader()
  }

  private def highlightOccupiedCells(): Unit = {
    def allStates[A](id: StateId[A]) = universes map (_.state(id))

    val occupiedCells = (UniverseComponent.All flatMap (_.position map allStates)).flatten.toSet
    for (cell <- occupiedCells) {
      drawRectangle(Transformation.create(cell.toVec2d, 0, 1), new Color(1, 1, 1, 0.3))
    }
  }

  private def drawShader(): Unit = {
    time += dt
    UniverseShader.setUniform("time", time.toFloat)
    var minValue = 0d
    for (universe <- universes) {
      val maxValue = minValue + universe.amplitude.squaredMagnitude

      frameBuffer.clear(CLEAR)
      (SpriteComponent.All.toList sortBy (_.layer)).foreach(_.draw(universe))
      Laser.All.toList.foreach(_.draw(universe))

      val camera = new Camera2d()
      camera.lowerLeft = new Vec2d(-1, -1)
      Camera.current = camera

      UniverseShader.setMVP(Transformation.IDENTITY)
      UniverseShader.setUniform("minVal", minValue.toFloat)
      UniverseShader.setUniform("maxVal", maxValue.toFloat)
      UniverseShader.setUniform("hue", (universe.amplitude.phase / (2 * Pi)).toFloat)
      UniverseShader.setUniform("color", new Vec4d(1, 1, 1, 1))
      Framebuffer.drawToWindow(colorBuffer, UniverseShader)

      UniverseShader.setUniform("minVal", 0f)
      UniverseShader.setUniform("maxVal", 1f)
      UniverseShader.setUniform("color", new Vec4d(1, 1, 1, 0.1))
      Framebuffer.drawToWindow(colorBuffer, UniverseShader)

      Camera.current = Camera.camera2d
      minValue = maxValue
    }
  }
}

private object Multiverse {
  private val UniverseShader: Shader = Shader.load(classOf[Multiverse].getResource(_), "shaders/universe")

  def declareSystem(): Unit = Game.declareSystem(classOf[Multiverse], (_: Multiverse).step())

  private def walls(tileMap: Tilemap): Set[Vec2i] =
    (for (layer <- tileMap.layers.asScala if layer.properties.asScala.get("Collision") exists (_.value.toBoolean);
          x <- 0 until layer.width;
          y <- 0 until layer.height if layer.data.tiles(x)(y) != 0) yield {
      Vec2i(
        (x + layer.offsetX.toDouble / tileMap.tileWidth).round.toInt,
        (y + layer.offsetY.toDouble / tileMap.tileHeight).round.toInt)
    }).toSet

  private def normalize(universes: List[Universe]): List[Universe] = {
    val sum = (universes map (_.amplitude.squaredMagnitude)).sum
    universes map (_ / Complex(sqrt(sum)))
  }

  private def combine(universes: List[Universe]): List[Universe] =
    universes
      .groupMapReduce(_.state)(identity)(_ + _.amplitude)
      .values
      .filter(_.amplitude.squaredMagnitude > 1e-6)
      .toList |>
      normalize
}
