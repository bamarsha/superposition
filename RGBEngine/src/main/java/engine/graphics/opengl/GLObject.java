package engine.graphics.opengl;

public abstract class GLObject {

    public final int id;

    public GLObject(int id) {
        this.id = id;
    }

    public abstract void bind();

    public static void bindAll(GLObject... a) {
        for (GLObject o : a) {
            o.bind();
        }
    }

    public abstract void destroy();
}
