package engine.graphics.sprites

import engine.graphics.opengl.BufferObject
import engine.graphics.opengl.GLObject.Companion.bindAll
import engine.graphics.opengl.Shader
import engine.graphics.opengl.Texture
import engine.graphics.opengl.VertexArrayObject
import engine.util.Color
import engine.util.Transformation
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import java.util.*

class Sprite private constructor(fileName: String) {

    private val texture: Texture

    val height: Int
        get() = texture.height

    val width: Int
        get() = texture.width

    init {
        this.texture = Texture.load(fileName)
    }

    fun draw(t: Transformation, color: Color) {
        drawTexture(texture, t, color)
    }

    companion object {

        private val SPRITE_CACHE = HashMap<String, Sprite>()

        fun load(fileName: String): Sprite {
            if (!SPRITE_CACHE.containsKey(fileName)) {
                val s = Sprite(fileName)
                SPRITE_CACHE.put(fileName, s)
            }
            return SPRITE_CACHE.get(fileName)!!
        }

        val SPRITE_SHADER = Shader.load("sprite")

        val SPRITE_VAO = VertexArrayObject.createVAO {
            val vbo = BufferObject(GL_ARRAY_BUFFER, floatArrayOf(0.5f, 0.5f, 0f, 1f, 1f, 0.5f, -0.5f, 0f, 1f, 0f, -0.5f, -0.5f, 0f, 0f, 0f, -0.5f, 0.5f, 0f, 0f, 1f))
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 20, 0)
            glEnableVertexAttribArray(0)
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 20, 12)
            glEnableVertexAttribArray(1)
        }

        fun drawTexture(texture: Texture, t: Transformation, color: Color) {
            SPRITE_SHADER.setMVP(t)
            SPRITE_SHADER.setUniform("color", color)
            bindAll(texture, SPRITE_SHADER, SPRITE_VAO)
            glDrawArrays(GL_TRIANGLE_FAN, 0, 4)
        }
    }
}
