package superposition.game.system

import com.badlogic.ashley.core._
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20.{GL_BLEND, GL_COLOR_BUFFER_BIT}
import com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.graphics.glutils.{FrameBuffer, ShaderProgram, ShapeRenderer}
import com.badlogic.gdx.math.Matrix4
import superposition.game.ResourceResolver.resolve
import superposition.game.component.{Beam, Multiverse, Position, SpriteView}
import superposition.quantum.Universe

import scala.jdk.CollectionConverters._
import scala.math.Pi

final class MultiverseRenderer extends EntitySystem {
  private val defaultBatch: SpriteBatch = new SpriteBatch

  private val universeShader: ShaderProgram = new ShaderProgram(
    resolve("shaders/universe.vert"),
    resolve("shaders/universe.frag"))

  private val universeBatch: SpriteBatch = new SpriteBatch(1000, universeShader)

  private val shapeRenderer: ShapeRenderer = new ShapeRenderer

  // TODO: Frame buffer dimensions?
  private val frameBuffer: FrameBuffer = new FrameBuffer(RGBA8888, 1280, 720, false)

  private var multiverses: Iterable[Entity] = Nil

  private var time: Float = 0

  override def addedToEngine(engine: Engine): Unit =
    multiverses = engine.getEntitiesFor(Family.all(classOf[Multiverse]).get).asScala

  override def update(deltaTime: Float): Unit = {
    time += deltaTime
    for (multiverse <- multiverses map Multiverse.Mapper.get) {
      highlightOccupiedCells(multiverse)

      val spriteViews = (multiverse.entities filter SpriteView.Mapper.has map SpriteView.Mapper.get)
        .toSeq sortBy (_.layer)
      val beams = multiverse.entities filter Beam.Mapper.has map Beam.Mapper.get
      var minValue: Float = 0
      for (universe <- multiverse.universes) {
        frameBuffer.begin()
        gl.glClearColor(0, 0, 0, 0)
        gl.glClear(GL_COLOR_BUFFER_BIT)
        drawSprites(multiverse.camera.combined, universe, spriteViews)
        beams.foreach(_.draw(universe))
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
        entity <- multiverse.entities if Position.Mapper.has(entity)
        position = Position.Mapper.get(entity)
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

  private def drawSprites(projection: Matrix4, universe: Universe, spriteViews: Seq[SpriteView]): Unit = {
    defaultBatch.setProjectionMatrix(projection)
    defaultBatch.begin()
    spriteViews.foreach(_.draw(defaultBatch, universe))
    defaultBatch.end()
  }
}
