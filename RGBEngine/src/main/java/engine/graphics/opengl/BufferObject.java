package engine.graphics.opengl;

import static org.lwjgl.opengl.GL15.*;

public class BufferObject extends GLObject {

    final int type;

    public BufferObject(int type, float[] data) {
        this(type);
        putData(data);
    }

    public BufferObject(int type, int[] data) {
        this(type);
        putData(data);
    }

    public BufferObject(int type) {
        super(glGenBuffers());
        this.type = type;
    }

    @Override
    public void bind() {
        GLState.bindBuffer(this);
    }

    @Override
    public void destroy() {
        glDeleteBuffers(id);
    }

    public final void putData(float[] data) {
        putData(data, GL_STATIC_DRAW);
    }

    public final void putData(float[] data, int usage) {
        bind();
        glBufferData(type, data, usage);
    }

    public final void putData(int[] data) {
        putData(data, GL_STATIC_DRAW);
    }

    public final void putData(int[] data, int usage) {
        bind();
        glBufferData(type, data, usage);
    }
}
