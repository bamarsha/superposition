/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package engine.graphics.sprites;

import engine.graphics.opengl.Shader;
import engine.graphics.opengl.Texture;
import engine.util.Color;
import engine.util.math.Transformation;
import engine.util.math.Vec2d;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static engine.graphics.opengl.GLObject.bindAll;
import static engine.graphics.sprites.Sprite.SPRITE_VAO;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.glDrawArrays;

/**
 *
 * @author TARS
 */
public class SpriteSheet {

    public static final int SHEET_DEPTH = 32;

    private static final Map<URL, SpriteSheet> SPRITE_SHEET_CACHE = new HashMap();

    public static SpriteSheet load(URL url) {
        if (!SPRITE_SHEET_CACHE.containsKey(url)) {
            SpriteSheet s = new SpriteSheet(url);
            SPRITE_SHEET_CACHE.put(url, s);
        }
        return SPRITE_SHEET_CACHE.get(url);
    }

    public static final Shader SPRITE_SHEET_SHADER = Shader.load(SpriteSheet.class::getResource, "sprite_sheet");

    private final Texture texture;

    private SpriteSheet(URL url) {
        this.texture = Texture.load(url);
    }

    public void draw(Transformation t, int id, Color color) {
        drawTexture(texture, t, id, color);
    }

    public static void drawTexture(Texture texture, Transformation t, int id, Color color) {
        SPRITE_SHEET_SHADER.setMVP(t);
        SPRITE_SHEET_SHADER.setUniform("color", color);
        SPRITE_SHEET_SHADER.setUniform("subCoords", new Vec2d(id % SHEET_DEPTH, 15 - id / SHEET_DEPTH));
        bindAll(texture, SPRITE_SHEET_SHADER, SPRITE_VAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
    }

    public int getHeight() {
        return texture.getHeight();
    }

    public int getWidth() {
        return texture.getWidth();
    }
}
