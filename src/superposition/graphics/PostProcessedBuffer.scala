package superposition.graphics

import com.badlogic.gdx.Gdx.{gl, graphics}
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT
import com.badlogic.gdx.graphics.Pixmap.Format.RGB888
import com.badlogic.gdx.graphics.g2d.{Batch, SpriteBatch}
import com.badlogic.gdx.graphics.glutils.{FloatFrameBuffer, FrameBuffer, ShaderProgram}
import superposition.game.ResourceResolver.resolve

/** A frame buffer with a post-processing shader program.
  *
  * @param fragmentShader the fragment shader to use
  * @param useFloats true to use a [[com.badlogic.gdx.graphics.glutils.FloatFrameBuffer]] instead of a
  * [[com.badlogic.gdx.graphics.glutils.FrameBuffer]]
  */
final class PostProcessedBuffer(fragmentShader: FileHandle, useFloats: Boolean = false) {
  /** The post-processing shader program. */
  val shader: ShaderProgram = new ShaderProgram(resolve("shaders/sprite.vert"), fragmentShader)

  /** The post-processing batch. */
  val batch: Batch = new SpriteBatch(1000, shader)

  // TODO: Resize the frame buffer if the window is resized.
  /** The post-processed frame buffer. */
  val buffer: FrameBuffer =
    if (useFloats)
      new FloatFrameBuffer(graphics.getWidth, graphics.getHeight, false)
    else new FrameBuffer(RGB888, graphics.getWidth, graphics.getHeight, false)

  /** Clears the buffer. */
  def clear(): Unit = {
    buffer.begin()
    gl.glClearColor(0, 0, 0, 0)
    gl.glClear(GL_COLOR_BUFFER_BIT)
    buffer.end()
  }

  /** Draws the buffer to the batch.
    *
    * @param batch the batch
    * @param camera the camera
    */
  def draw(batch: Batch, camera: Camera): Unit =
    batch.draw(buffer.getColorBufferTexture, 0, camera.viewportHeight, camera.viewportWidth, -camera.viewportHeight)

  /** Captures the rendering action in the buffer.
    *
    * @param camera the camera
    * @param render the rendering action
    */
  def capture(camera: Camera)(render: () => Unit): Unit = {
    buffer.begin()
    batch.begin()
    batch.setProjectionMatrix(camera.combined)
    render()
    batch.end()
    buffer.end()
  }
}
