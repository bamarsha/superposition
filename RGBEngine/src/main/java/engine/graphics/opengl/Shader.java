package engine.graphics.opengl;

import engine.graphics.Camera;
import java.util.HashMap;
import org.joml.Matrix4d;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import engine.util.Color;
import engine.util.Resources;
import engine.util.math.Transformation;
import engine.util.math.Vec2d;
import engine.util.math.Vec3d;
import engine.util.math.Vec4d;

public class Shader extends GLObject {

    private final HashMap<String, Integer> uniformLocations = new HashMap();

    private Shader(String vertexShaderSource, String geometryShaderSource, String fragmentShaderSource) {
        super(glCreateProgram());

        attach(GL_VERTEX_SHADER, vertexShaderSource);
        attach(GL_GEOMETRY_SHADER, geometryShaderSource);
        attach(GL_FRAGMENT_SHADER, fragmentShaderSource);

        glLinkProgram(id);
        if (glGetProgrami(id, GL_LINK_STATUS) != GL_TRUE) {
            throw new RuntimeException("Shader program doesn't link:\n" + glGetProgramInfoLog(id));
        }
    }

    private void attach(int type, String source) {
        if (source != null) {
            int shader = glCreateShader(type);
            glShaderSource(shader, source);
            glCompileShader(shader);
            if (glGetShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE) {
                throw new RuntimeException("Shader doesn't compile:\n" + glGetShaderInfoLog(shader));
            }
            glAttachShader(id, shader);
            glDeleteShader(shader);
        }
    }

    @Override
    public void bind() {
        GLState.bindShader(this);
    }

    @Override
    public void destroy() {
        glDeleteProgram(id);
    }

    private int getUniformLocation(String name) {
        if (!uniformLocations.containsKey(name)) {
            uniformLocations.put(name, glGetUniformLocation(id, name));
        }
        return uniformLocations.get(name);
    }

    public static Shader load(String name) {
        return load(name, name);
    }

    public static Shader loadGeom(String name) {
        return loadGeom(name, name, name);
    }

    public static Shader load(String vert, String frag) {
        return loadGeom(vert, null, frag);
    }

    public static Shader loadGeom(String vert, String geom, String frag) {
        return new Shader(
                vert == null ? null : Resources.loadFileAsString("src/main/java/shaders/" + vert + ".vert"),
                geom == null ? null : Resources.loadFileAsString("src/main/java/shaders/" + geom + ".geom"),
                frag == null ? null : Resources.loadFileAsString("src/main/java/shaders/" + frag + ".frag"));
    }

    public void setMVP(Transformation t) {
        setUniform("model", t.matrix());
        setUniform("view", Camera.current.viewMatrix());
        setUniform("projection", Camera.current.projectionMatrix());
    }

    public void setUniform(String name, boolean value) {
        setUniform(name, value ? 1 : 0);
    }

    public void setUniform(String name, int value) {
        bind();
        int uniform = getUniformLocation(name);
        glUniform1i(uniform, value);
    }

    public void setUniform(String name, float value) {
        bind();
        int uniform = getUniformLocation(name);
        glUniform1f(uniform, value);
    }

    public void setUniform(String name, Vec2d value) {
        bind();
        int uniform = getUniformLocation(name);
        glUniform2fv(uniform, new float[]{(float) value.x, (float) value.y});
    }

    public void setUniform(String name, Vec3d value) {
        bind();
        int uniform = getUniformLocation(name);
        glUniform3fv(uniform, new float[]{(float) value.x, (float) value.y, (float) value.z});
    }

    public void setUniform(String name, Vec4d value) {
        bind();
        int uniform = getUniformLocation(name);
        glUniform4fv(uniform, new float[]{(float) value.x, (float) value.y, (float) value.z, (float) value.w});
    }

    public void setUniform(String name, Color value) {
        bind();
        int uniform = getUniformLocation(name);
        glUniform4fv(uniform, new float[]{(float) value.r, (float) value.g, (float) value.b, (float) value.a});
    }

    public void setUniform(String name, Matrix4d mat) {
        bind();
        int uniform = getUniformLocation(name);
        glUniformMatrix4fv(uniform, false, new float[]{
            (float) mat.m00(), (float) mat.m01(), (float) mat.m02(), (float) mat.m03(),
            (float) mat.m10(), (float) mat.m11(), (float) mat.m12(), (float) mat.m13(),
            (float) mat.m20(), (float) mat.m21(), (float) mat.m22(), (float) mat.m23(),
            (float) mat.m30(), (float) mat.m31(), (float) mat.m32(), (float) mat.m33()});
    }
}
