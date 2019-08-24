package engine.graphics.opengl

import engine.core.Settings
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL15.glBindBuffer
import org.lwjgl.opengl.GL20.glUseProgram
import org.lwjgl.opengl.GL30.*
import java.util.*

class GLState {

    private var blendFunc1: Int = 0
    private var blendFunc2: Int = 0
    private val buffers = HashMap<Int, BufferObject>()
    private val flags = HashMap<Int, Boolean>()
    private var framebuffer: Framebuffer? = null
    private var shader: Shader? = null
    private var texture = arrayOfNulls<Texture>(32)
    private var vao: VertexArrayObject? = null

    private fun copy(): GLState {
        val copy = GLState()
        copy.blendFunc1 = blendFunc1
        copy.blendFunc2 = blendFunc2
        copy.buffers.putAll(buffers)
        copy.flags.putAll(flags)
        copy.framebuffer = framebuffer
        copy.shader = shader
        copy.texture = texture
        copy.vao = vao
        return copy
    }

    override fun toString(): String {
        return "GLState{blendFunc1=$blendFunc1, blendFunc2=$blendFunc2, buffers=$buffers, flags=$flags, framebuffer=$framebuffer, shader=$shader, texture=$texture, vao=$vao}"
    }

    companion object {

        private val state = GLState()

        fun bindBuffer(buffer: BufferObject) {
            if (state.buffers.get(buffer.type) !== buffer) {
                state.buffers.put(buffer.type, buffer)
                glBindBuffer(buffer.type, buffer.id)
            }
        }

        fun bindFramebuffer(framebuffer: Framebuffer?) {
            if (framebuffer !== state.framebuffer) {
                state.framebuffer = framebuffer
                glBindFramebuffer(GL_FRAMEBUFFER, framebuffer?.id ?: 0)
                glViewport(0, 0, framebuffer?.width ?: Settings.WINDOW_WIDTH,
                        framebuffer?.height ?: Settings.WINDOW_HEIGHT)
            }
        }

        fun bindShader(shader: Shader?) {
            if (shader !== state.shader) {
                state.shader = shader
                glUseProgram(shader?.id ?: 0)
            }
        }

        fun bindTexture(texture: Texture?) {
            if (texture != null) {
                glActiveTexture(GL_TEXTURE0 + texture.num)
                if (texture !== state.texture[texture.num]) {
                    state.texture[texture.num] = texture
                    glBindTexture(texture.type, texture.id)
                }
            }
        }

        fun bindTexture(texture: Texture?, num: Int) {
            glActiveTexture(GL_TEXTURE0 + num)
            if (texture !== state.texture[num]) {
                state.texture[num] = texture
                if (texture != null) {
                    glBindTexture(texture.type, texture.id)
                }
            }
        }

        fun bindVertexArrayObject(vao: VertexArrayObject?) {
            if (vao !== state.vao) {
                state.vao = vao
                glBindVertexArray(vao?.id ?: 0)
            }
        }

        fun disable(vararg flags: Int) {
            for (flag in flags) {
                setFlag(flag, false)
            }
        }

        fun enable(vararg flags: Int) {
            for (flag in flags) {
                setFlag(flag, true)
            }
        }

        fun getBuffer(type: Int): BufferObject {
            return state.buffers.get(type)!!
        }

        fun getFlag(flag: Int): Boolean? {
            return state.flags.get(flag)
        }

        fun getFramebuffer(): Framebuffer? {
            return state.framebuffer
        }

        val shaderProgram: Shader?
            get() = state.shader

        fun getTexture(num: Int): Texture {
            return state.texture[num]!!
        }

        val vertexArrayObject: VertexArrayObject?
            get() = state.vao

        fun setBlendFunc(blendFunc1: Int, blendFunc2: Int) {
            if (blendFunc1 != state.blendFunc1 || blendFunc2 != state.blendFunc2) {
                state.blendFunc1 = blendFunc1
                state.blendFunc2 = blendFunc2
                glBlendFunc(blendFunc1, blendFunc2)
            }
        }

        fun setFlag(flag: Int, value: Boolean) {
            if (state.flags.get(flag) != value) {
                state.flags.put(flag, value)
                if (value) {
                    glEnable(flag)
                } else {
                    glDisable(flag)
                }
            }
        }
    }
}
