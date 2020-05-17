package superposition.system

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.Gdx.{gl, graphics}
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20.GL_BLEND
import com.badlogic.gdx.graphics.g2d.{Batch, BitmapFont, GlyphLayout, SpriteBatch}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Disposable
import superposition.entity.Level

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

  override def update(deltaTime: Float): Unit = {
    val multiverse = level().get.multiverse

    val header = multiverse.stateIds.map(_.name)
    val allPrints = multiverse.universes.map(multiverse.printUniverse).transpose
    val important = allPrints.map(l => l.exists(_ != l.head))
    val widths = allPrints.zip(header).map(l => (l._1 :+ l._2).map { s =>
      val layout = new GlyphLayout()
      layout.setText(font, s)
      layout.width + 10f
    }.max)
    val xs = widths.scanLeft(16f)(_ + _)
    val ys = multiverse.universes.map(_ => -20f).scanLeft(graphics.getHeight - 16f)(_ + _)
    val minValues = multiverse.universes.map(_.amplitude.squaredMagnitude.toFloat).scanLeft(0f)(_ + _)

    gl.glEnable(GL_BLEND)
    shapeRenderer.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, graphics.getWidth(), graphics.getHeight()))
    shapeRenderer.begin(ShapeType.Filled)
    for ((y, minValue) <- ys.zip(None +: minValues.map(Some(_)))) {
      val color = new Color().fromHsv(minValue.map(_ * 360f).getOrElse(0f), if (minValue.isDefined) 1 else 0, 1)
      color.a = .5f
      shapeRenderer.setColor(color)
      shapeRenderer.rect(12f, y + 2, widths.sum - 2, -16)
    }
    shapeRenderer.end()

    batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, graphics.getWidth(), graphics.getHeight()))
    batch.begin()
    for ((((h, toPrint), i), x) <- header.zip(allPrints).zip(important).zip(xs)) {
      font.setColor(if (i) Color.WHITE else new Color(0x9f9f9fff))
      for ((y, p) <- ys.zip(h +: toPrint)) {
        font.draw(batch, p, x, y)
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
