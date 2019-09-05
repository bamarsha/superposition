package engine.graphics.opengl;

import engine.util.math.Vec2d;
import engine.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.lwjgl.opengl.ARBVertexArrayObject.glDeleteVertexArrays;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

public class VertexArrayObject extends GLObject {

//    public static VertexArrayObject createVAO(Runnable r) {
//        VertexArrayObject vao = new VertexArrayObject();
//        vao.bind();
//        r.run();
//        return vao;
//    }

    public VertexArrayObject() {
        super(glGenVertexArrays());
    }

    public static VertexArrayObject createVAO(Supplier<VAOBuilder> s) {
        return s.get().toVAO();
    }

    @Override
    public void bind() {
        GLState.bindVertexArrayObject(this);
    }

    @Override
    public void destroy() {
        glDeleteVertexArrays(id);
    }

    public static class VAOBuilder {
        private final int[] attribs;
        private final List<Float>[] data;

        public VAOBuilder(int... attribs) {
            if (attribs == null || attribs.length == 0) {
                throw new IllegalArgumentException("Trying to create empty VAO");
            }
            this.attribs = attribs;
            data = new List[attribs.length];
            for (int i = 0; i < attribs.length; i++) {
                data[i] = new ArrayList<>();
            }
        }

        public void add(int i, double... values) {
            if (attribs[i] != values.length) {
                throw new IllegalArgumentException("Wrong number of values");
            }
            for (var value : values) {
                data[i].add((float) value);
            }
        }

        public void add(int i, Vec2d v) {
            add(i, v.x, v.y);
        }

        public void add(int i, Vec3d v) {
            add(i, v.x, v.y, v.z);
        }

        public void addQuad(int i, Vec2d v, Vec2d d1, Vec2d d2) {
            add(i, v);
            add(i, v.add(d1));
            add(i, v.add(d1).add(d2));
            add(i, v);
            add(i, v.add(d2));
            add(i, v.add(d1).add(d2));
        }

        public void addQuad(int i, Vec3d v, Vec3d d1, Vec3d d2) {
            add(i, v);
            add(i, v.add(d1));
            add(i, v.add(d1).add(d2));
            add(i, v);
            add(i, v.add(d2));
            add(i, v.add(d1).add(d2));
        }

        public int numVertices() {
            return data[0].size() / attribs[0];
        }

        public VertexArrayObject toVAO() {
            int vertexSize = IntStream.of(attribs).sum();
            int numVertices = numVertices();
            float[] d = new float[vertexSize * numVertices];
            int idx = 0;
            for (int i = 0; i < numVertices; i++) {
                for (int j = 0; j < attribs.length; j++) {
                    for (int k = 0; k < attribs[j]; k++) {
                        d[idx] = data[j].get(i * attribs[j] + k);
                        idx++;
                    }
                }
            }
            if (idx != d.length) {
                throw new IllegalStateException("Didn't exactly fill data array - error in VAO definition");
            }

            VertexArrayObject vao = new VertexArrayObject();
            vao.bind();
            BufferObject vbo = new BufferObject(GL_ARRAY_BUFFER, d);
            int partialSum = 0;
            for (int i = 0; i < attribs.length; i++) {
                glVertexAttribPointer(i, attribs[i], GL_FLOAT, false, vertexSize * 4, partialSum * 4);
                glEnableVertexAttribArray(i);
                partialSum += attribs[i];
            }
            return vao;
        }
    }
}
