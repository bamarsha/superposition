package engine.graphics;

import engine.graphics.opengl.BufferObject;
import static engine.graphics.opengl.GLObject.bindAll;
import engine.graphics.opengl.Shader;
import engine.graphics.opengl.VertexArrayObject;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import engine.util.Color;
import engine.util.math.MathUtils;
import static engine.util.math.MathUtils.rotate;
import engine.util.math.Transformation;
import engine.util.math.Vec2d;
import engine.util.math.Vec3d;

public class Graphics {

    private static final Shader COLOR_SHADER = Shader.load("color");

    private static final int CIRCLE_DETAIL = 40;

    private static final VertexArrayObject CIRCLE_VAO = VertexArrayObject.createVAO(() -> {
        float circleVertices[] = new float[CIRCLE_DETAIL * 3 + 6];
        for (int i = 0; i <= CIRCLE_DETAIL; i++) {
            circleVertices[3 * i + 3] = (float) Math.cos(i * 2 * Math.PI / CIRCLE_DETAIL);
            circleVertices[3 * i + 4] = (float) Math.sin(i * 2 * Math.PI / CIRCLE_DETAIL);
        }
        BufferObject vbo = new BufferObject(GL_ARRAY_BUFFER, circleVertices);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 12, 0);
        glEnableVertexAttribArray(0);
    });

    private static final VertexArrayObject LINE_VAO = VertexArrayObject.createVAO(() -> {
        BufferObject vbo = new BufferObject(GL_ARRAY_BUFFER, new float[]{0, 0, 0, 1, 0, 0});
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 12, 0);
        glEnableVertexAttribArray(0);
    });

    public static void drawCircle(Vec2d center, double size, Color color) {
        COLOR_SHADER.setMVP(Transformation.create(center, 0, size));
        COLOR_SHADER.setUniform("color", color);
        bindAll(COLOR_SHADER, CIRCLE_VAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, CIRCLE_DETAIL + 2);
    }

    public static void drawCircleOutline(Vec2d center, double size, Color color) {
        for (int i = 0; i < CIRCLE_DETAIL; i++) {
            drawLine(center.add(MathUtils.rotate(new Vec2d(size, 0), Math.PI * 2 * i / CIRCLE_DETAIL)),
                    center.add(MathUtils.rotate(new Vec2d(size, 0), Math.PI * 2 * (i + 1) / CIRCLE_DETAIL)), color);
        }
    }

    public static void drawLine(Vec2d p1, Vec2d p2, Color color) {
        COLOR_SHADER.setMVP(Transformation.create(p1, p2.sub(p1), new Vec2d(0, 0)));
        COLOR_SHADER.setUniform("color", color);
        bindAll(COLOR_SHADER, LINE_VAO);
        glDrawArrays(GL_LINES, 0, 2);
    }

    public static void drawLine(Vec3d p1, Vec3d p2, Color color) {
        COLOR_SHADER.setMVP(Transformation.create(p1, p2.sub(p1), new Vec3d(0, 0, 0), new Vec3d(0, 0, 0)));
        COLOR_SHADER.setUniform("color", color);
        bindAll(COLOR_SHADER, LINE_VAO);
        glDrawArrays(GL_LINES, 0, 2);
    }

    private static final VertexArrayObject RECTANGLE_VAO = VertexArrayObject.createVAO(() -> {
        BufferObject vbo = new BufferObject(GL_ARRAY_BUFFER, new float[]{0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0});
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 12, 0);
        glEnableVertexAttribArray(0);
    });

    public static void drawRectangle(Transformation t, Color color) {
        COLOR_SHADER.setMVP(t);
        COLOR_SHADER.setUniform("color", color);
        bindAll(COLOR_SHADER, RECTANGLE_VAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
    }

//    public static void drawRectangle3d(Vec3d position, Vec3d normal, double rotation, Vec2d size, Vec4d color) {
//        COLOR_SHADER.setUniform("projectionMatrix", Camera.camera3d.getProjectionMatrix());
//        if (normal.x != 0 || normal.y != 0) {
//            COLOR_SHADER.setUniform("modelViewMatrix", Camera.camera3d.getWorldMatrix(position)
//                    .rotateTowards(normal.toJOML(), Camera.camera3d.up.toJOML()).rotate(rotation, normal.toJOML()).scale(new Vector3d(size.x, size.y, 1)));
//        } else {
//            COLOR_SHADER.setUniform("modelViewMatrix", Camera.camera3d.getWorldMatrix(position)
//                    .rotate(rotation, normal.toJOML()).scale(new Vector3d(size.x, size.y, 1)));
//        }
//        COLOR_SHADER.setUniform("color", color);
//        bindAll(COLOR_SHADER, RECTANGLE_VAO);
//        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
//    }
    public static void drawRectangleOutline(Transformation t, Color color) {
        Vec2d p1 = t.apply(new Vec2d(0, 0));
        Vec2d p2 = t.apply(new Vec2d(1, 0));
        Vec2d p3 = t.apply(new Vec2d(1, 1));
        Vec2d p4 = t.apply(new Vec2d(0, 1));
//        Vec2d p1 = position;
//        Vec2d p2 = rotate(new Vec2d(size.x, 0), rotation).add(position);
//        Vec2d p3 = rotate(size, rotation).add(position);
//        Vec2d p4 = rotate(new Vec2d(0, size.y), rotation).add(position);
        drawLine(p1, p2, color);
        drawLine(p2, p3, color);
        drawLine(p3, p4, color);
        drawLine(p4, p1, color);
    }

    public static void drawWideLine(Vec2d p1, Vec2d p2, double width, Color color) {
        Vec2d delta = p2.sub(p1);
        Vec2d perp = rotate(delta, Math.PI / 2).setLength(-width / 2);
        drawRectangle(Transformation.create(p1.add(perp), delta, perp.mul(2)), color);
    }
}
