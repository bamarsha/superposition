package engine.graphics.opengl;

import engine.core.Settings;
import engine.util.Color;
import engine.util.math.Vec2d;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class Framebuffer extends GLObject {

    public static final VertexArrayObject FRAMEBUFFER_VAO = VertexArrayObject.createVAO(() -> {
        var b = new VertexArrayObject.VAOBuilder(2, 2);
        b.addQuad(0, new Vec2d(-1, -1), new Vec2d(2, 0), new Vec2d(0, 2));
        b.addQuad(1, new Vec2d(0, 0), new Vec2d(1, 0), new Vec2d(0, 1));
        return b;
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

    public static void drawToWindow(Texture texture, Shader shader) {
        GLState.bindFramebuffer(null);
        bindAll(texture, shader, FRAMEBUFFER_VAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }

    public void drawToSelf(Texture texture, Shader shader) {
        bindAll(this, texture, shader, FRAMEBUFFER_VAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }
}
