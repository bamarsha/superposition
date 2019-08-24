package engine.graphics.opengl

import engine.graphics.Camera
import engine.util.*
import org.joml.Matrix4d
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER
import java.util.*

class Shader private constructor(vertexShaderSource: String?, geometryShaderSource: String?, fragmentShaderSource: String?) : GLObject(glCreateProgram()) {

    private val uniformLocations = HashMap<String, Int>()

    init {

        attach(GL_VERTEX_SHADER, vertexShaderSource)
        attach(GL_GEOMETRY_SHADER, geometryShaderSource)
        attach(GL_FRAGMENT_SHADER, fragmentShaderSource)

        glLinkProgram(id)
        if (glGetProgrami(id, GL_LINK_STATUS) != GL11.GL_TRUE) {
            throw RuntimeException("Shader program doesn't link:\n" + glGetProgramInfoLog(id))
        }
    }

    private fun attach(type: Int, source: String?) {
        if (source != null) {
            val shader = glCreateShader(type)
            glShaderSource(shader, source)
            glCompileShader(shader)
            if (glGetShaderi(shader, GL_COMPILE_STATUS) != GL11.GL_TRUE) {
                throw RuntimeException("Shader doesn't compile:\n" + glGetShaderInfoLog(shader))
            }
            glAttachShader(id, shader)
            glDeleteShader(shader)
        }
    }

    override fun bind() {
        GLState.bindShader(this)
    }

    override fun destroy() {
        glDeleteProgram(id)
    }

    private fun getUniformLocation(name: String): Int {
        if (!uniformLocations.containsKey(name)) {
            uniformLocations[name] = glGetUniformLocation(id, name)
        }
        return uniformLocations[name]!!
    }

    fun setMVP(t: Transformation) {
        setUniform("model", t.matrix())
        setUniform("view", Camera.current.viewMatrix())
        setUniform("projection", Camera.current.projectionMatrix())
    }

    fun setUniform(name: String, value: Boolean) {
        setUniform(name, if (value) 1 else 0)
    }

    fun setUniform(name: String, value: Int) {
        bind()
        val uniform = getUniformLocation(name)
        glUniform1i(uniform, value)
    }

    fun setUniform(name: String, value: Float) {
        bind()
        val uniform = getUniformLocation(name)
        glUniform1f(uniform, value)
    }

    fun setUniform(name: String, value: Vec2d) {
        bind()
        val uniform = getUniformLocation(name)
        glUniform2fv(uniform, floatArrayOf(value.x.toFloat(), value.y.toFloat()))
    }

    fun setUniform(name: String, value: Vec3d) {
        bind()
        val uniform = getUniformLocation(name)
        glUniform3fv(uniform, floatArrayOf(value.x.toFloat(), value.y.toFloat(), value.z.toFloat()))
    }

    fun setUniform(name: String, value: Color) {
        bind()
        val uniform = getUniformLocation(name)
        glUniform4fv(uniform, floatArrayOf(value.r.toFloat(), value.g.toFloat(), value.b.toFloat(), value.a.toFloat()))
    }

    fun setUniform(name: String, mat: Matrix4d) {
        bind()
        val uniform = getUniformLocation(name)
        glUniformMatrix4fv(uniform, false, floatArrayOf(
                mat.m00().toFloat(), mat.m01().toFloat(), mat.m02().toFloat(), mat.m03().toFloat(),
                mat.m10().toFloat(), mat.m11().toFloat(), mat.m12().toFloat(), mat.m13().toFloat(),
                mat.m20().toFloat(), mat.m21().toFloat(), mat.m22().toFloat(), mat.m23().toFloat(),
                mat.m30().toFloat(), mat.m31().toFloat(), mat.m32().toFloat(), mat.m33().toFloat()))
    }

    companion object {

        fun load(name: String): Shader {
            return load(name, name)
        }

        fun loadGeom(name: String): Shader {
            return loadGeom(name, name, name)
        }

        fun load(vert: String, frag: String): Shader {
            return loadGeom(vert, null, frag)
        }

        fun loadGeom(vert: String?, geom: String?, frag: String?): Shader {
            return Shader(
                    if (vert == null) null else Resources.loadFileAsString("src/main/java/shaders/$vert.vert"),
                    if (geom == null) null else Resources.loadFileAsString("src/main/java/shaders/$geom.geom"),
                    if (frag == null) null else Resources.loadFileAsString("src/main/java/shaders/$frag.frag"))
        }
    }
}
