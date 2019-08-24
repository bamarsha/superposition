package engine.graphics.opengl

import org.lwjgl.opengl.ARBTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LEVEL
import org.lwjgl.opengl.GL30.glGenerateMipmap
import org.lwjgl.stb.STBImage.*
import java.nio.ByteBuffer

class Texture(internal val type: Int) : GLObject(glGenTextures()) {
    var width: Int = 0
        private set
    var height: Int = 0
        private set
    var num: Int = 0

    override fun bind() {
        GLState.bindTexture(this)
    }

    override fun destroy() {
        glDeleteTextures(id)
    }

    fun setParameter(name: Int, value: Int) {
        bind()
        glTexParameteri(type, name, value)
    }

    fun setParameter(name: Int, value: FloatArray) {
        bind()
        glTexParameterfv(type, name, value)
    }

    fun uploadData(width: Int, height: Int, data: ByteBuffer) {
        this.width = width
        this.height = height
        bind()
        glTexImage2D(type, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data)
        glGenerateMipmap(type)
    }

    companion object {

        fun load(fileName: String): Texture {
            val widthArray = IntArray(1)
            val heightArray = IntArray(1)
            val compArray = IntArray(1)
            stbi_set_flip_vertically_on_load(true)
            val image = stbi_load("sprites/$fileName", widthArray, heightArray, compArray, 4)
                    ?: throw RuntimeException("Failed to load image " + fileName + " : " + stbi_failure_reason())

            val t = Texture(GL_TEXTURE_2D)
            t.setParameter(GL_TEXTURE_MAX_LEVEL, 16)
            t.setParameter(GL_TEXTURE_MAX_ANISOTROPY, 16)
            t.uploadData(widthArray[0], heightArray[0], image)
            return t
        }
    }
}
