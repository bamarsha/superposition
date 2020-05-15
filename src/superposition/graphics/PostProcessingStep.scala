package superposition.graphics

import com.badlogic.gdx.Gdx.{gl, graphics}
import com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT
import com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.{FloatFrameBuffer, FrameBuffer, ShaderProgram}
import com.badlogic.gdx.graphics.{Camera, Color}
import superposition.game.ResourceResolver.resolve

class PostProcessingStep(fragShaderName: String = "sprite", useFloats: Boolean = false) {

  val shader: ShaderProgram = new ShaderProgram(
    resolve("shaders/sprite.vert"),
    resolve("shaders/" + fragShaderName + ".frag"))
  assert(shader.isCompiled)

  val batch: SpriteBatch = new SpriteBatch(1000, shader)


  // TODO: Resize the frame buffer if the window is resized.
  val buffer: FrameBuffer = if (useFloats)
    new FloatFrameBuffer(graphics.getWidth, graphics.getHeight, false)
  else
    new FrameBuffer(RGBA8888, graphics.getWidth, graphics.getHeight, false)

  var clearColor: Color = new Color(0, 0, 0, 0)

  var camera: Camera = _

  def clear(): Unit = {
    buffer.begin()
    gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a)
    gl.glClear(GL_COLOR_BUFFER_BIT)
    buffer.end()
  }

  def drawTo(batch: SpriteBatch): Unit = {
    batch.draw(buffer.getColorBufferTexture, 0, camera.viewportHeight, camera.viewportWidth, -camera.viewportHeight)
  }

  def run(action: => Unit): Unit = {
    buffer.begin()
    batch.begin()
    batch.setProjectionMatrix(camera.combined)
    action
    batch.end()
    buffer.end()
  }
}
