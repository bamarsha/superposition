package superposition.graphics

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShaderProgram

object Extensions {

  implicit final class ColorOps(val self: Color) extends AnyVal {
    def mixWith(c2: Color): Color = {
      val c = new Color(self)
      c.lerp(c2, c2.a)
      c.a = self.a
      c
    }

    def toFloatArray: Array[Float] = Array(self.r, self.g, self.b, self.a)
  }

  implicit final class ShaderOps(val self: ShaderProgram) extends AnyVal {
    def setUniformColor(name: String, color: Color): Unit = {
      self.setUniform4fv(name, color.toFloatArray, 0, 4)
    }
  }

}
