package superposition.system

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx.{gl, graphics}
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Color.WHITE
import com.badlogic.gdx.graphics.GL20.GL_BLEND
import com.badlogic.gdx.graphics.g2d.{Batch, BitmapFont, GlyphLayout, SpriteBatch}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Disposable
import superposition.entity.Level
import superposition.system.StateDisplaySystem.{ImportantColor, NormalColor}

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

  override def update(deltaTime: Float): Unit = {
    val multiverse = level().get.multiverse
    val headers = multiverse.stateIds.view map (_.name)
    val columns = (multiverse.universes.view map multiverse.printUniverse).transpose
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

    gl.glEnable(GL_BLEND)
    shapeRenderer.setProjectionMatrix((new Matrix4).setToOrtho2D(0, 0, graphics.getWidth, graphics.getHeight))
    shapeRenderer.begin(Filled)
    drawRectangle(WHITE, ys.head, totalWidth)
    for ((y, minValue) <- ys.tail.zip(minValues)) {
      drawRectangle((new Color).fromHsv(minValue * 360, 1, 1), y, totalWidth)
    }
    shapeRenderer.end()

    batch.setProjectionMatrix((new Matrix4).setToOrtho2D(0, 0, graphics.getWidth, graphics.getHeight))
    batch.begin()
    for ((((header, column), important), x) <- headers.zip(columns).zip(importants).zip(xs)) {
      font.setColor(if (important) ImportantColor else NormalColor)
      font.draw(batch, header, x, ys.head)
      for ((y, cell) <- ys.tail.zip(column)) {
        font.draw(batch, cell, x, y)
      }
    }
    batch.end()
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
  }
}

/** Constants for the multiverse state display system. */
private object StateDisplaySystem {
  /** The font color for normal state values. */
  private val NormalColor = new Color(0x9f9f9fff)

  /** The font color for important state values. */
  private val ImportantColor = WHITE
}
