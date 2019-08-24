package engine.graphics.opengl

import org.lwjgl.opengl.ARBVertexArrayObject.glDeleteVertexArrays
import org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays

class VertexArrayObject private constructor() : GLObject(glGenVertexArrays()) {

    override fun bind() {
        GLState.bindVertexArrayObject(this)
    }

    override fun destroy() {
        glDeleteVertexArrays(id)
    }

    companion object {

        fun createVAO(r: () -> Unit): VertexArrayObject {
            val vao = VertexArrayObject()
            vao.bind()
            r()
            return vao
        }
    }
}
