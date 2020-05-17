package superposition.graphics

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShaderProgram

/** Utility functions for colors. */
private object ColorUtils {

  /** Operations for [[com.badlogic.gdx.graphics.Color]].
    *
    * @param color the color
    */
  implicit final class ColorOps(val color: Color) extends AnyVal {
    /** Mixes this color with the other color and returns the mixed color.
      *
      * @param other the color to mix with
      * @return the mixed color
      */
    def mixed(other: Color): Color = {
      val result = color.cpy.lerp(other, other.a)
      result.a = color.a
      result
    }
  }

  /** Operations for [[com.badlogic.gdx.graphics.glutils.ShaderProgram]].
    *
    * @param shader the shader
    */
  implicit final class ShaderOps(val shader: ShaderProgram) extends AnyVal {
    /** Sets the uniform to the color.
      *
      * @param name the name of the uniform
      * @param color the color to set the uniform to
      */
    def setUniformColor(name: String, color: Color): Unit =
      shader.setUniform4fv(name, Array(color.r, color.g, color.b, color.a), 0, 4)
  }

}
