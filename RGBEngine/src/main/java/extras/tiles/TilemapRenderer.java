package extras.tiles;

import engine.graphics.opengl.BufferObject;
import engine.graphics.opengl.Texture;
import engine.graphics.opengl.VertexArrayObject;
import engine.util.Color;
import engine.util.math.Transformation;
import engine.util.math.Vec2d;
import engine.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static engine.graphics.opengl.GLObject.bindAll;
import static engine.graphics.sprites.Sprite.SPRITE_SHADER;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

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

        List<Vec3d> vertices = new ArrayList<>();
        List<Vec2d> texCoords = new ArrayList<>();
        for (var layer : tilemap.layers) {
            for (int x = 0; x < layer.width; x++) {
                for (int y = 0; y < layer.height; y++) {
                    int tileId = layer.data.tiles[x][y];
                    if (tileId > 0) {
                        int pos = tileId - 1;
                        float xx = x + (float) layer.offsetX / tilemap.tileWidth;
                        float yy = y - (float) layer.offsetY / tilemap.tileHeight;
                        float dx = (float) tilemap.tileWidth / image.width;
                        float dy = (float) tilemap.tileHeight / image.height;
                        float tx = dx * (pos % tileset.columns);
                        float ty = (1 - dy * (pos / tileset.columns + 1));
                        vertices.add(new Vec3d(xx, yy, 0));
                        vertices.add(new Vec3d(xx + 1, yy, 0));
                        vertices.add(new Vec3d(xx + 1, yy + 1, 0));
                        vertices.add(new Vec3d(xx, yy, 0));
                        vertices.add(new Vec3d(xx, yy + 1, 0));
                        vertices.add(new Vec3d(xx + 1, yy + 1, 0));
                        texCoords.add(new Vec2d(tx, ty));
                        texCoords.add(new Vec2d(tx + dx, ty));
                        texCoords.add(new Vec2d(tx + dx, ty + dy));
                        texCoords.add(new Vec2d(tx, ty));
                        texCoords.add(new Vec2d(tx, ty + dy));
                        texCoords.add(new Vec2d(tx + dx, ty + dy));
                    }
                }
            }
        }
        numVertices = vertices.size();

        float[] data = new float[numVertices * 5];
        for (int i = 0; i < numVertices; i++) {
            System.arraycopy(new float[]{
                    (float) vertices.get(i).x, (float) vertices.get(i).y, (float) vertices.get(i).z,
                    (float) texCoords.get(i).x, (float) texCoords.get(i).y
            }, 0, data, i * 5, 5);
        }

        vao = VertexArrayObject.createVAO(() -> {
            BufferObject vbo = new BufferObject(GL_ARRAY_BUFFER, data);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 20, 0);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 20, 12);
            glEnableVertexAttribArray(1);
        });
    }

    public void draw(Transformation t, Color color) {
        SPRITE_SHADER.setMVP(t);
        SPRITE_SHADER.setUniform("color", color);
        bindAll(texture, SPRITE_SHADER, vao);
        glDrawArrays(GL_TRIANGLES, 0, numVertices);
    }
}
