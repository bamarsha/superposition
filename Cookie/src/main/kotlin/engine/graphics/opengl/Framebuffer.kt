package engine.graphics.opengl

import engine.core.Settings
import engine.util.Color
import engine.util.Vec2d
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.*

class Framebuffer @JvmOverloads constructor(val width: Int = Settings.WINDOW_WIDTH, val height: Int = Settings.WINDOW_HEIGHT) : GLObject(glGenFramebuffers()) {

    constructor(size: Vec2d) : this(size.x as Int, size.y as Int)

    fun attachColorBuffer(): Texture {
        return attachTexture(GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE, GL_LINEAR, GL_COLOR_ATTACHMENT0)
    }

    fun attachDepthRenderbuffer() {
        val rboDepth = glGenRenderbuffers()
        glBindRenderbuffer(GL_RENDERBUFFER, rboDepth)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height)
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboDepth)
    }

    fun attachDepthStencilBuffer(): Texture {
        return attachTexture(GL_DEPTH24_STENCIL8, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, GL_LINEAR, GL_DEPTH_STENCIL_ATTACHMENT)
    }

    fun attachTexture(gpuFormat: Int, storageType: Int, cpuFormat: Int, filterType: Int, attachmentType: Int): Texture {
        bind()
        val t = Texture(GL_TEXTURE_2D)
        t.bind()
        glTexImage2D(GL_TEXTURE_2D, 0, gpuFormat, width, height, 0, storageType, cpuFormat, 0)
        t.setParameter(GL_TEXTURE_MIN_FILTER, filterType)
        t.setParameter(GL_TEXTURE_MAG_FILTER, filterType)
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachmentType, GL_TEXTURE_2D, t.id, 0)
        return t
    }

    override fun bind() {
        GLState.bindFramebuffer(this)
    }

    fun clear(color: Color) {
        bind()
        glClearColor(color.r.toFloat(), color.g.toFloat(), color.b.toFloat(), color.a.toFloat())
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
    }

    override fun destroy() {
        glDeleteFramebuffers(id)
    }

    fun drawToSelf(texture: Texture, shader: Shader) {
        GLObject.bindAll(this, texture, shader, FRAMEBUFFER_VAO)
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4)
    }

    companion object {

        val FRAMEBUFFER_VAO = VertexArrayObject.createVAO {
            val vbo = BufferObject(GL_ARRAY_BUFFER, floatArrayOf(-1f, -1f, 0f, 0f, 1f, -1f, 1f, 0f, 1f, 1f, 1f, 1f, -1f, 1f, 0f, 1f))
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 16, 0)
            glEnableVertexAttribArray(0)
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 16, 8)
            glEnableVertexAttribArray(1)
        }

        fun clearWindow(color: Color) {
            GLState.bindFramebuffer(null)
            glClearColor(color.r.toFloat(), color.g.toFloat(), color.b.toFloat(), color.a.toFloat())
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
        }

        fun drawToWindow(texture: Texture, shader: Shader) {
            GLState.bindFramebuffer(null)
            GLObject.bindAll(texture, shader, FRAMEBUFFER_VAO)
            glDrawArrays(GL_TRIANGLE_FAN, 0, 4)
        }
    }
}
