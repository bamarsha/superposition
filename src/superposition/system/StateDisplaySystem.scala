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
    val widths =
      (columns.zip(headers) map { case (column, header) =>
        (Iterable(header) ++ column).map(text => {
          glyphLayout.setText(font, text)
          glyphLayout.width + 10f
        }).max
      }).toSeq
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
    for ((y, minValue) <- ys.zip(Iterable(None) ++ (minValues map Some.apply))) {
      val color = (new Color).fromHsv(minValue.getOrElse(0f) * 360f, if (minValue.isDefined) 1 else 0, 1)
      color.a = 0.5f
      shapeRenderer.setColor(color)
      shapeRenderer.rect(12f, y + 2, widths.sum - 2, -16)
    }
    shapeRenderer.end()

    batch.setProjectionMatrix((new Matrix4).setToOrtho2D(0, 0, graphics.getWidth, graphics.getHeight))
    batch.begin()
    for ((((header, column), important), x) <- headers.zip(columns).zip(importants).zip(xs)) {
      font.setColor(if (important) ImportantColor else NormalColor)
      for ((y, cell) <- ys.zip(Iterable(header) ++ column)) {
        font.draw(batch, cell, x, y)
      }
    }
    batch.end()
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
