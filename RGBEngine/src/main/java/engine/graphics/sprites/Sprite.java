package engine.graphics.sprites;

import engine.graphics.opengl.Shader;
import engine.graphics.opengl.Texture;
import engine.graphics.opengl.VertexArrayObject;
import engine.util.Color;
import engine.util.math.Transformation;
import engine.util.math.Vec2d;
import engine.util.math.Vec3d;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static engine.graphics.opengl.GLObject.bindAll;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;

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
        var b = new VertexArrayObject.VAOBuilder(3, 2);
        b.addQuad(0, new Vec3d(-.5, -.5, 0), new Vec3d(1, 0, 0), new Vec3d(0, 1, 0));
        b.addQuad(1, new Vec2d(0, 0), new Vec2d(1, 0), new Vec2d(0, 1));
        return b;
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
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }

    public int getHeight() {
        return texture.getHeight();
    }

    public int getWidth() {
        return texture.getWidth();
    }
}
