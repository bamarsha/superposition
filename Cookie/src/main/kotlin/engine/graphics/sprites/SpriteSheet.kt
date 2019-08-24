/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package engine.graphics.sprites

import engine.graphics.opengl.BufferObject
import engine.graphics.opengl.GLObject.Companion.bindAll
import engine.graphics.opengl.Shader
import engine.graphics.opengl.Texture
import engine.graphics.opengl.VertexArrayObject
import engine.util.Color
import engine.util.Transformation
import engine.util.Vec2d
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import java.util.*

/**
 *
 * @author TARS
 */
class SpriteSheet private constructor(fileName: String) {

    private val texture: Texture

    val height: Int
        get() = texture.height

    val width: Int
        get() = texture.width

    init {
        this.texture = Texture.load(fileName)
    }

    fun draw(t: Transformation, id: Int, color: Color) {
        drawTexture(texture, t, id, color)
    }

    companion object {

        val SHEET_DEPTH = 32

        private val SPRITE_SHEET_CACHE = HashMap<String, SpriteSheet>()

        fun load(fileName: String): SpriteSheet {
            if (!SPRITE_SHEET_CACHE.containsKey(fileName)) {
                val s = SpriteSheet(fileName)
                SPRITE_SHEET_CACHE.put(fileName, s)
            }
            return SPRITE_SHEET_CACHE.get(fileName)!!
        }

        val SPRITE_SHEET_SHADER = Shader.load("sprite_sheet")

        val SPRITE_SHEET_VAO = VertexArrayObject.createVAO {
            val vbo = BufferObject(GL_ARRAY_BUFFER, floatArrayOf(0.5f, 0.5f, 0f, 1f, 1f, 0.5f, -0.5f, 0f, 1f, 0f, -0.5f, -0.5f, 0f, 0f, 0f, -0.5f, 0.5f, 0f, 0f, 1f))
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 20, 0)
            glEnableVertexAttribArray(0)
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 20, 12)
            glEnableVertexAttribArray(1)
        }

        fun drawTexture(texture: Texture, t: Transformation, id: Int, color: Color) {
            SPRITE_SHEET_SHADER.setMVP(t)
            SPRITE_SHEET_SHADER.setUniform("color", color)
            SPRITE_SHEET_SHADER.setUniform("subCoords", Vec2d((id % SHEET_DEPTH).toDouble(), (15 - id / SHEET_DEPTH).toDouble()))
            bindAll(texture, SPRITE_SHEET_SHADER, SPRITE_SHEET_VAO)
            glDrawArrays(GL_TRIANGLE_FAN, 0, 4)
        }
    }
}
