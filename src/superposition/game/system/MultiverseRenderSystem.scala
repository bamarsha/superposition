package superposition.game.system

import com.badlogic.ashley.core._
import com.badlogic.gdx.Gdx.{gl, graphics}
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20.{GL_BLEND, GL_COLOR_BUFFER_BIT}
import com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.graphics.glutils.{FrameBuffer, ShaderProgram, ShapeRenderer}
import com.badlogic.gdx.math.Matrix4
import superposition.game.ResourceResolver.resolve
import superposition.game.component._
import superposition.quantum.Universe

import scala.jdk.CollectionConverters._
import scala.math.Pi

final class MultiverseRenderSystem extends EntitySystem {
  private val defaultBatch: SpriteBatch = new SpriteBatch

  private val universeShader: ShaderProgram = new ShaderProgram(
    resolve("shaders/universe.vert"),
    resolve("shaders/universe.frag"))

  private val universeBatch: SpriteBatch = new SpriteBatch(1000, universeShader)

  private val shapeRenderer: ShapeRenderer = new ShapeRenderer

  // TODO: Resize the framebuffer if the window is resized.
  private val frameBuffer: FrameBuffer = new FrameBuffer(RGBA8888, graphics.getWidth, graphics.getHeight, false)

  private var multiverses: Iterable[Entity] = Nil

  private var time: Float = 0

  override def addedToEngine(engine: Engine): Unit =
    multiverses = engine.getEntitiesFor(Family.all(classOf[Multiverse]).get).asScala

  override def update(deltaTime: Float): Unit = {
    time += deltaTime
    for (multiverse <- multiverses map Multiverse.Mapper.get) {
      highlightOccupiedCells(multiverse)

      val spriteEntities =
        (multiverse.entities filter { entity =>
          SpriteView.Mapper.has(entity) &&
            (ClassicalPosition.Mapper.has(entity) || QuantumPosition.Mapper.has(entity))
        }).toSeq sortBy (SpriteView.Mapper.get(_).layer)
      val beamEntities = multiverse.entities filter { entity =>
        Beam.Mapper.has(entity) && ClassicalPosition.Mapper.has(entity)
      }
      var minValue: Float = 0
      for (universe <- multiverse.universes) {
        frameBuffer.begin()
        gl.glClearColor(0, 0, 0, 0)
        gl.glClear(GL_COLOR_BUFFER_BIT)
        drawSprites(multiverse.camera.combined, universe, spriteEntities)
        for (entity <- beamEntities) {
          Beam.Mapper.get(entity).draw(universe, ClassicalPosition.Mapper.get(entity).cell)
        }
        frameBuffer.end()

        val maxValue = minValue + universe.amplitude.squaredMagnitude.toFloat
        drawBufferedUniverse(multiverse.camera, universe, minValue, maxValue)
        minValue = maxValue
      }
    }
  }

  private def drawBufferedUniverse(camera: Camera, universe: Universe, minValue: Float, maxValue: Float): Unit = {
    universeBatch.setProjectionMatrix(camera.combined)
    drawBufferedUniverseWith(camera) {
      universeShader.setUniformf("time", time)
      universeShader.setUniformf("minVal", minValue)
      universeShader.setUniformf("maxVal", maxValue)
      universeShader.setUniformf("hue", (universe.amplitude.phase / (2 * Pi)).toFloat)
      universeShader.setUniform4fv("color", Array(1, 1, 1, 1), 0, 4)
    }
    drawBufferedUniverseWith(camera) {
      universeShader.setUniformf("minVal", 0f)
      universeShader.setUniformf("maxVal", 1f)
      universeShader.setUniform4fv("color", Array(1, 1, 1, 0.1f), 0, 4)
    }
  }

  private def drawBufferedUniverseWith(camera: Camera)(setup: => Unit): Unit = {
    universeBatch.begin()
    setup
    universeBatch.draw(frameBuffer.getColorBufferTexture,
                       0,
                       camera.viewportHeight,
                       camera.viewportWidth,
                       -camera.viewportHeight)
    universeBatch.end()
  }

  private def highlightOccupiedCells(multiverse: Multiverse): Unit = {
    val occupiedCells =
      (for {
        entity <- multiverse.entities if QuantumPosition.Mapper.has(entity)
        position = QuantumPosition.Mapper.get(entity)
        universe <- multiverse.universes
      } yield universe.state(position.cell)).toSet

    gl.glEnable(GL_BLEND)
    shapeRenderer.setProjectionMatrix(multiverse.camera.combined)
    shapeRenderer.begin(ShapeType.Filled)
    shapeRenderer.setColor(1, 1, 1, 0.3f)
    for (cell <- occupiedCells) {
      shapeRenderer.rect(cell.x, cell.y, 1, 1)
    }
    shapeRenderer.end()
    gl.glDisable(GL_BLEND)
  }

  private def drawSprites(projection: Matrix4, universe: Universe, entities: Seq[Entity]): Unit = {
    defaultBatch.setProjectionMatrix(projection)
    defaultBatch.begin()
    for (entity <- entities) {
      val spriteView = SpriteView.Mapper.get(entity)
      val position =
        if (ClassicalPosition.Mapper.has(entity)) ClassicalPosition.Mapper.get(entity).absolute
        else universe.meta(QuantumPosition.Mapper.get(entity).absolute)
      spriteView.draw(defaultBatch, universe, position)
    }
    defaultBatch.end()
  }
}
