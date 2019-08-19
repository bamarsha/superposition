package engine.graphics.opengl;

import java.nio.ByteBuffer;
import static org.lwjgl.opengl.ARBTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LEVEL;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;

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

    public static Texture load(String fileName) {
        int[] widthArray = new int[1];
        int[] heightArray = new int[1];
        int[] compArray = new int[1];
        stbi_set_flip_vertically_on_load(true);
        ByteBuffer image = stbi_load("sprites/" + fileName, widthArray, heightArray, compArray, 4);
        if (image == null) {
            throw new RuntimeException("Failed to load image " + fileName + " : " + stbi_failure_reason());
        }

        Texture t = new Texture(GL_TEXTURE_2D);
        t.setParameter(GL_TEXTURE_MAX_LEVEL, 16);
        t.setParameter(GL_TEXTURE_MAX_ANISOTROPY, 16);
        t.uploadData(widthArray[0], heightArray[0], image);
        return t;
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
