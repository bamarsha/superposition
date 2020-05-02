package superposition.game

import com.badlogic.ashley.core._
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMap

/**
 * The multiverse is a collection of universes.
 *
 * Multiple universes represent qubits in superposition. The multiverse can apply quantum gates to qubits by changing
 * the amplitude of a universe or creating a copy of a universe.
 */
private final class Level(map: TiledMap) extends Entity {
  locally {
    val camera = new OrthographicCamera(map.getProperties.get("width", classOf[Int]),
                                        map.getProperties.get("height", classOf[Int]))
    camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0)
    camera.update()

    add(new MapView(map, camera))
    add(new Multiverse(camera))
  }

//  private var time: Double = 0
//
//  private var frameBuffer: Framebuffer = _
//
//  private var colorBuffer: Texture = _
//
//  add(new UniverseComponent(this, blockingCells = const(walls(tileMap))))
//
//  override protected def onCreate(): Unit = {
//    frameBuffer = new Framebuffer()
//    colorBuffer = frameBuffer.attachColorBuffer()
//  }
//
//  override protected def onDestroy(): Unit = entities.foreach(Game.destroy)
//
//  /**
//   * Applies a gate to the multiverse.
//   *
//   * If the gate produces any universe that is in an invalid state, no changes are made.
//   *
//   * @param gate  the gate to apply
//   * @param value the value to give the gate
//   * @return whether the gate was successfully applied
//   */
//  def applyGate[A](gate: Gate[A], value: A): Boolean = {
//    val newUniverses = gate.applyToAll(value)(universes)
//    if (newUniverses forall (_.isValid)) {
//      universes = newUniverses |>
//        combine |>
//        (_.toSeq) |>
//        (_ sortBy (universe => stateIds.reverse map (universe.state(_).toString)))
//      true
//    } else false
//  }
//
//  def forall(f: Universe => Boolean): Boolean = universes forall f
//

//  private def step(): Unit = {
//    tileRenderer.draw(Transformation.IDENTITY, Color.WHITE)
//    drawShader()
//  }

//
//  private def drawShader(): Unit = {
//    time += dt
//    UniverseShader.setUniform("time", time.toFloat)
//    var minValue = 0d
//    for (universe <- universes) {
//      val maxValue = minValue + universe.amplitude.squaredMagnitude
//
//      frameBuffer.clear(CLEAR)
//      (SpriteComponent.All.toList sortBy (_.layer)).foreach(_.draw(universe))
//      Laser.All.toList.foreach(_.draw(universe))
//
//      val camera = new Camera2d()
//      camera.lowerLeft = new Vec2d(-1, -1)
//      Camera.current = camera
//
//      UniverseShader.setMVP(Transformation.IDENTITY)
//      UniverseShader.setUniform("minVal", minValue.toFloat)
//      UniverseShader.setUniform("maxVal", maxValue.toFloat)
//      UniverseShader.setUniform("hue", (universe.amplitude.phase / (2 * Pi)).toFloat)
//      UniverseShader.setUniform("color", new Vec4d(1, 1, 1, 1))
//      Framebuffer.drawToWindow(colorBuffer, UniverseShader)
//
//      UniverseShader.setUniform("minVal", 0f)
//      UniverseShader.setUniform("maxVal", 1f)
//      UniverseShader.setUniform("color", new Vec4d(1, 1, 1, 0.1))
//      Framebuffer.drawToWindow(colorBuffer, UniverseShader)
//
//      Camera.current = Camera.camera2d
//      minValue = maxValue
//    }
//  }
}

//private object Multiverse {
//  private val UniverseShader: Shader = Shader.load(classOf[Multiverse].getResource(_), "shaders/universe")
//
//  def declareSystem(): Unit = Game.declareSystem(classOf[Multiverse], (_: Multiverse).step())
//
//  private def walls(tileMap: Tilemap): Set[Vec2i] =
//    (for (layer <- tileMap.layers.asScala if layer.properties.asScala.get("Collision") exists (_.value.toBoolean);
//          x <- 0 until layer.width;
//          y <- 0 until layer.height if layer.data.tiles(x)(y) != 0) yield {
//      Vec2i(
//        (x + layer.offsetX.toDouble / tileMap.tileWidth).round.toInt,
//        (y + layer.offsetY.toDouble / tileMap.tileHeight).round.toInt)
//    }).toSet
//
//  private def normalize(universes: Iterable[Universe]): Iterable[Universe] = {
//    val sum = (universes map (_.amplitude.squaredMagnitude)).sum
//    universes map (_ / Complex(sqrt(sum)))
//  }
//
//  private def combine(universes: Iterable[Universe]): Iterable[Universe] =
//    universes
//      .groupMapReduce(_.state)(identity)(_ + _.amplitude)
//      .values
//      .filter(_.amplitude.squaredMagnitude > 1e-6) |>
//      normalize
//}
