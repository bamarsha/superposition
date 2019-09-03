package extras.tiles;

import engine.graphics.opengl.Texture;
import engine.graphics.opengl.VertexArrayObject;
import engine.util.Color;
import engine.util.math.Transformation;
import engine.util.math.Vec2d;
import engine.util.math.Vec3d;

import java.util.function.Function;

import static engine.graphics.opengl.GLObject.bindAll;
import static engine.graphics.sprites.Sprite.SPRITE_SHADER;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;

public class TilemapRenderer {

    private final Texture texture;
    private final VertexArrayObject vao;
    private final int numVertices;

    public TilemapRenderer(Tilemap tilemap, Function<String, Texture> textureLoader) {
        if (tilemap.tilesets.size() != 1) {
            throw new IllegalArgumentException("TilemapRenderer assumes the map has a single tileset");
        }
        var tileset = tilemap.tilesets.get(0);
        if (tileset.images.size() != 1) {
            throw new IllegalArgumentException("TilemapRenderer assumes the tileset uses a spritesheet");
        }
        var image = tileset.images.get(0);
        texture = textureLoader.apply(image.source);

        VertexArrayObject.VAOBuilder b = new VertexArrayObject.VAOBuilder(3, 2);
        for (var layer : tilemap.layers) {
            for (int x = 0; x < layer.width; x++) {
                for (int y = 0; y < layer.height; y++) {
                    int tileId = layer.data.tiles[x][y];
                    if (tileId != 0) {
                        int flags = tileId >>> 29;
                        boolean horFlip = (flags & 4) > 0;
                        boolean verFlip = (flags & 2) > 0;
                        boolean diagFlip = (flags & 1) > 0;

                        int pos = (tileId & 0x1FFFFFFF) - 1;
                        float xx = x + (float) layer.offsetX / tilemap.tileWidth;
                        float yy = y - (float) layer.offsetY / tilemap.tileHeight;
                        b.addQuad(0, new Vec3d(xx, yy, 0), new Vec3d(1, 0, 0), new Vec3d(0, 1, 0));

                        float dx = (float) tilemap.tileWidth / image.width;
                        float dy = (float) tilemap.tileHeight / image.height;
                        float tx = dx * (pos % tileset.columns);
                        float ty = (1 - dy * (pos / tileset.columns + 1));
                        Vec2d v = new Vec2d(tx, ty);
                        Vec2d d1 = new Vec2d(dx, 0);
                        Vec2d d2 = new Vec2d(0, dy);
                        if (diagFlip) {
                            var temp = d1;
                            d1 = d2;
                            d2 = temp;
                        }
                        if (horFlip) {
                            v = v.add(d1);
                            d1 = d1.mul(-1);
                        }
                        if (verFlip) {
                            v = v.add(d2);
                            d2 = d2.mul(-1);
                        }
                        b.addQuad(1, v, d1, d2);
                    }
                }
            }
        }
        numVertices = b.numVertices();
        vao = b.toVAO();
    }

    public void draw(Transformation t, Color color) {
        SPRITE_SHADER.setMVP(t);
        SPRITE_SHADER.setUniform("color", color);
        bindAll(texture, SPRITE_SHADER, vao);
        glDrawArrays(GL_TRIANGLES, 0, numVertices);
    }
}
