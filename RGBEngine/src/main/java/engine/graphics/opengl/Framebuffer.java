package engine.graphics.opengl;

import engine.core.Settings;
import engine.util.Color;
import engine.util.math.Vec2d;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class Framebuffer extends GLObject {

    public static final VertexArrayObject FRAMEBUFFER_VAO = VertexArrayObject.createVAO(() -> {
        BufferObject vbo = new BufferObject(GL_ARRAY_BUFFER, new float[]{
            -1, -1, 0, 0,
            1, -1, 1, 0,
            1, 1, 1, 1,
            -1, 1, 0, 1
        });
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 16, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 16, 8);
        glEnableVertexAttribArray(1);
    });

    public final int width, height;

    public Framebuffer(int width, int height) {
        super(glGenFramebuffers());
        this.width = width;
        this.height = height;
    }

    public Framebuffer(Vec2d size) {
        this((int) size.x, (int) size.y);
    }

    public Framebuffer() {
        this(Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT);
    }

    public Texture attachColorBuffer() {
        return attachTexture(GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE, GL_LINEAR, GL_COLOR_ATTACHMENT0);
    }

    public void attachDepthRenderbuffer() {
        int rboDepth = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, rboDepth);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboDepth);
    }

    public Texture attachDepthStencilBuffer() {
        return attachTexture(GL_DEPTH24_STENCIL8, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, GL_LINEAR, GL_DEPTH_STENCIL_ATTACHMENT);
    }

    public Texture attachTexture(int gpuFormat, int storageType, int cpuFormat, int filterType, int attachmentType) {
        bind();
        Texture t = new Texture(GL_TEXTURE_2D);
        t.bind();
        glTexImage2D(GL_TEXTURE_2D, 0, gpuFormat, width, height, 0, storageType, cpuFormat, 0);
        t.setParameter(GL_TEXTURE_MIN_FILTER, filterType);
        t.setParameter(GL_TEXTURE_MAG_FILTER, filterType);
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachmentType, GL_TEXTURE_2D, t.id, 0);
        return t;
    }

    @Override
    public void bind() {
        GLState.bindFramebuffer(this);
    }

    public void clear(Color color) {
        bind();
        glClearColor((float) color.r, (float) color.g, (float) color.b, (float) color.a);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    public static void clearWindow(Color color) {
        GLState.bindFramebuffer(null);
        glClearColor((float) color.r, (float) color.g, (float) color.b, (float) color.a);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    @Override
    public void destroy() {
        glDeleteFramebuffers(id);
    }

    public void drawToSelf(Texture texture, Shader shader) {
        bindAll(this, texture, shader, FRAMEBUFFER_VAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
    }

    public static void drawToWindow(Texture texture, Shader shader) {
        GLState.bindFramebuffer(null);
        bindAll(texture, shader, FRAMEBUFFER_VAO);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
    }
}
