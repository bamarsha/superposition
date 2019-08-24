package engine.graphics.opengl

import org.lwjgl.opengl.GL15.*

class BufferObject(internal val type: Int) : GLObject(glGenBuffers()) {

    constructor(type: Int, data: FloatArray) : this(type) {
        putData(data)
    }

    constructor(type: Int, data: IntArray) : this(type) {
        putData(data)
    }

    override fun bind() {
        GLState.bindBuffer(this)
    }

    override fun destroy() {
        glDeleteBuffers(id)
    }

    @JvmOverloads
    fun putData(data: FloatArray, usage: Int = GL_STATIC_DRAW) {
        bind()
        glBufferData(type, data, usage)
    }

    @JvmOverloads
    fun putData(data: IntArray, usage: Int = GL_STATIC_DRAW) {
        bind()
        glBufferData(type, data, usage)
    }
}
