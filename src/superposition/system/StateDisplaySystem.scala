package superposition.system

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx.{gl, graphics}
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Color.WHITE
import com.badlogic.gdx.graphics.GL20._
import com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888
import com.badlogic.gdx.graphics.g2d.{Batch, BitmapFont, GlyphLayout, SpriteBatch}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled
import com.badlogic.gdx.graphics.glutils.{FrameBuffer, ShapeRenderer}
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Disposable
import superposition.entity.Level
import superposition.math.Universe
import superposition.system.StateDisplaySystem.{importantColor, normalColor}

import scala.Function.const

/** Displays the state of the multiverse.
  *
  * @param level a function that returns the current level
  */
final class StateDisplaySystem(level: () => Option[Level]) extends EntitySystem with Disposable {
  /** The shape renderer. */
  private val shapeRenderer: ShapeRenderer = new ShapeRenderer

  /** The batch. */
  private val batch: Batch = new SpriteBatch

  /** The display font. */
  private val font: BitmapFont = new BitmapFont

  /** A glyph layout. */
  private val glyphLayout: GlyphLayout = new GlyphLayout

  // TODO: Resize the frame buffer if the window is resized.
  /** The frame buffer. */
  private val buffer: FrameBuffer = new FrameBuffer(RGBA8888, graphics.getWidth, graphics.getHeight, false)

  /** The projection matrix. */
  private val projection: Matrix4 = (new Matrix4).setToOrtho2D(0, 0, graphics.getWidth, graphics.getHeight)

  /** The universes in the multiverse when the state display was last updated. */
  private var lastUniverses: Seq[Universe] = Nil

  override def update(deltaTime: Float): Unit = {
    val universes = level().get.multiverse.universes
    val changed = universes.view
      .zipAll(lastUniverses, Universe.empty, Universe.empty)
      .exists(((_: Universe).state != (_: Universe).state).tupled)
    if (changed) {
      drawState()
    }
    lastUniverses = universes

    batch.begin()
    batch.draw(buffer.getColorBufferTexture, 0, graphics.getHeight, graphics.getWidth, -graphics.getHeight)
    batch.end()
  }

  /** Draws the state table to the frame buffer. */
  private def drawState(): Unit = {
    val multiverse = level().get.multiverse
    val headers = multiverse.stateIds.view map (_.name)
    val columns = (multiverse.universes.view map multiverse.showUniverse).transpose
    val importants = columns map (column => column exists (_ != column.head))
    val widths = columns.zip(headers) map { case (column, header) => textWidth(header).max((column map textWidth).max) }
    val totalWidth = widths.sum
    val xs = widths.scanLeft(16f)(_ + _)
    val ys = multiverse.universes.view
      .map(const(-20f))
      .scanLeft(graphics.getHeight - 16f)(_ + _)
    val minValues = multiverse.universes.view
      .map(_.amplitude.squaredMagnitude.toFloat)
      .scanLeft(0f)(_ + _)

    buffer.begin()
    gl.glClearColor(0, 0, 0, 0)
    gl.glClear(GL_COLOR_BUFFER_BIT)
    gl.glDisable(GL_BLEND)

    shapeRenderer.setProjectionMatrix(projection)
    shapeRenderer.begin(Filled)
    drawRectangle(WHITE, ys.head, totalWidth)
    for ((y, minValue) <- ys.tail.zip(minValues)) {
      drawRectangle((new Color).fromHsv(minValue * 360, 1, 1), y, totalWidth)
    }
    shapeRenderer.end()

    batch.setProjectionMatrix(projection)
    batch.begin()
    for ((((header, column), important), x) <- headers.zip(columns).zip(importants).zip(xs)) {
      font.setColor(if (important) importantColor else normalColor)
      font.draw(batch, header, x, ys.head)
      for ((y, cell) <- ys.tail.zip(column)) {
        font.draw(batch, cell, x, y)
      }
    }
    batch.end()

    gl.glEnable(GL_BLEND)
    buffer.end()
  }

  /** Returns the width of the text in the display font.
    *
    * @param text the text
    * @return the width of the text
    */
  private def textWidth(text: String): Float = {
    glyphLayout.setText(font, text)
    glyphLayout.width + 10f
  }

  /** Draws a rectangle in the table.
    *
    * @param color the color
    * @param y the y coordinate
    * @param width the width
    */
  private def drawRectangle(color: Color, y: Float, width: Float): Unit = {
    shapeRenderer.setColor(color.r, color.g, color.b, 0.5f)
    shapeRenderer.rect(12f, y + 2, width - 2, -16)
  }

  override def dispose(): Unit = {
    shapeRenderer.dispose()
    batch.dispose()
    font.dispose()
    buffer.dispose()
  }
}

/** Constants for the multiverse state display system. */
private object StateDisplaySystem {
  /** The font color for normal state values. */
  private val normalColor = new Color(0x9f9f9fff)

  /** The font color for important state values. */
  private val importantColor = WHITE
}
