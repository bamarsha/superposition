package engine.graphics.sprites;

import engine.graphics.opengl.BufferObject;
import static engine.graphics.opengl.GLObject.bindAll;
import engine.graphics.opengl.Shader;
import engine.graphics.opengl.Texture;
import engine.graphics.opengl.VertexArrayObject;
import engine.util.Color;
import engine.util.math.Transformation;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

public class Sprite {

    private static final Map<URL, Sprite> SPRITE_CACHE = new HashMap();

    public static Sprite load(URL url) {
        if (!SPRITE_CACHE.containsKey(url)) {
            Sprite s = new Sprite(url);
            SPRITE_CACHE.put(url, s);
        }
        return SPRITE_CACHE.get(url);
    }

    public static final Shader SPRITE_SHADER = Shader.load(Sprite.class::getResource, "sprite");

    public static final VertexArrayObject SPRITE_VAO = VertexArrayObject.createVAO(() -> {
        BufferObject vbo = new BufferObject(GL_ARRAY_BUFFER, new float[]{
            0.5f, 0.5f, 0, 1, 1,
            0.5f, -0.5f, 0, 1, 0,
            -0.5f, -0.5f, 0, 0, 0,
            -0.5f, 0.5f, 0, 0, 1
        });
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 20, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 20, 12);
        glEnableVertexAttribArray(1);
    });

    private final Texture texture;

    private Sprite(URL url) {
        this.texture = Texture.load(url);
    }

    public void draw(Transformation t, Color color) {
        drawTexture(texture, t, color);
    }

    public static void drawTexture(Texture texture, Transformation t, Color color) {
        SPRITE_SHADER.setMVP(t);
        SPRITE_SHADER.setUniform("color", color);
        bindAll(texture, SPRITE_SHADER, SPRITE_VAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
    }

    public int getHeight() {
        return texture.getHeight();
    }

    public int getWidth() {
        return texture.getWidth();
    }
}
