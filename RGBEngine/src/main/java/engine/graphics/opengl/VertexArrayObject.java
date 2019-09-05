package engine.graphics.opengl;

import static org.lwjgl.opengl.ARBVertexArrayObject.*;

public class VertexArrayObject extends GLObject {

    public static VertexArrayObject createVAO(Runnable r) {
        VertexArrayObject vao = new VertexArrayObject();
        vao.bind();
        r.run();
        return vao;
    }

    private VertexArrayObject() {
        super(glGenVertexArrays());
    }

    @Override
    public void bind() {
        GLState.bindVertexArrayObject(this);
    }

    @Override
    public void destroy() {
        glDeleteVertexArrays(id);
    }
}
