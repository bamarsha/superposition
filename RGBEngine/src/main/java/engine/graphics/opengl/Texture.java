package engine.graphics.opengl;

import engine.util.Resources;
import org.lwjgl.system.MemoryStack;

import java.net.URL;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.ARBTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LEVEL;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class Texture extends GLObject {

    final int type;
    private int width, height;
    public int num;

    public Texture(int type) {
        super(glGenTextures());
        this.type = type;
    }

    @Override
    public void bind() {
        GLState.bindTexture(this);
    }

    @Override
    public void destroy() {
        glDeleteTextures(id);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public static Texture load(URL url) {
        stbi_set_flip_vertically_on_load(true);

        try (var stack = MemoryStack.stackPush()) {
            var buffer = stack.bytes(Resources.loadBytes(url));
            var width = stack.mallocInt(1);
            var height = stack.mallocInt(1);
            var channels = stack.mallocInt(1);

            ByteBuffer image = stbi_load_from_memory(buffer, width, height, channels, 4);
            if (image == null) {
                throw new RuntimeException("Failed to load image " + url + ": " + stbi_failure_reason());
            }

            Texture t = new Texture(GL_TEXTURE_2D);
            t.setParameter(GL_TEXTURE_MAX_LEVEL, 16);
            t.setParameter(GL_TEXTURE_MAX_ANISOTROPY, 16);
            t.setParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            t.uploadData(width.get(0), height.get(0), image);
            return t;
        }
    }

    public void setParameter(int name, int value) {
        bind();
        glTexParameteri(type, name, value);
    }

    public void setParameter(int name, float[] value) {
        bind();
        glTexParameterfv(type, name, value);
    }

    public void uploadData(int width, int height, ByteBuffer data) {
        this.width = width;
        this.height = height;
        bind();
        glTexImage2D(type, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
        glGenerateMipmap(type);
    }
}
