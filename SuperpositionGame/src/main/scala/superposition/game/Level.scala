package superposition.game

import com.badlogic.ashley.core._
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.{TiledMap, TiledMapTileLayer}
import superposition.game.Level.walls
import superposition.math.Vector2i

import scala.jdk.CollectionConverters._

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
    add(new Multiverse(walls(map), camera))
  }

//  private var time: Double = 0
//
//  private var frameBuffer: Framebuffer = _
//
//  private var colorBuffer: Texture = _
//
//  override protected def onCreate(): Unit = {
//    frameBuffer = new Framebuffer()
//    colorBuffer = frameBuffer.attachColorBuffer()
//  }
//
//  override protected def onDestroy(): Unit = entities.foreach(Game.destroy)
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

private object Level {
  //  private val UniverseShader: Shader = Shader.load(classOf[Multiverse].getResource(_), "shaders/universe")

  private def walls(map: TiledMap): Set[Vector2i] =
    (for (layer <- map.getLayers.asScala if layer.isInstanceOf[TiledMapTileLayer] && hasCollision(layer);
          tiledLayer = layer.asInstanceOf[TiledMapTileLayer];
          x <- 0 until tiledLayer.getWidth;
          y <- 0 until tiledLayer.getHeight if hasTileAt(tiledLayer, x, y)) yield {
      Vector2i(
        (x + layer.getOffsetX.toDouble / map.getProperties.get("tilewidth", classOf[Int])).round.toInt,
        (y + layer.getOffsetY.toDouble / map.getProperties.get("tileheight", classOf[Int])).round.toInt)
    }).toSet

  private def hasCollision(layer: MapLayer): Boolean =
    layer.getProperties.containsKey("Collision") && layer.getProperties.get("Collision", classOf[Boolean])

  private def hasTileAt(layer: TiledMapTileLayer, x: Int, y: Int): Boolean = Option(layer.getCell(x, y)).isDefined
}
