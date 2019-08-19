package engine.graphics.opengl;

import engine.core.Settings;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class GLState {

    private static GLState state = new GLState();

    private int blendFunc1, blendFunc2;
    private final Map<Integer, BufferObject> buffers = new HashMap();
    private final Map<Integer, Boolean> flags = new HashMap();
    private Framebuffer framebuffer = null;
    private Shader shader = null;
    private Texture[] texture = new Texture[32];
    private VertexArrayObject vao = null;

    public static void bindBuffer(BufferObject buffer) {
        if (state.buffers.get(buffer.type) != buffer) {
            state.buffers.put(buffer.type, buffer);
            glBindBuffer(buffer.type, buffer.id);
        }
    }

    public static void bindFramebuffer(Framebuffer framebuffer) {
        if (framebuffer != state.framebuffer) {
            state.framebuffer = framebuffer;
            glBindFramebuffer(GL_FRAMEBUFFER, framebuffer == null ? 0 : framebuffer.id);
            glViewport(0, 0, framebuffer == null ? Settings.WINDOW_WIDTH : framebuffer.width,
                    framebuffer == null ? Settings.WINDOW_HEIGHT : framebuffer.height);
        }
    }

    public static void bindShader(Shader shader) {
        if (shader != state.shader) {
            state.shader = shader;
            glUseProgram(shader == null ? 0 : shader.id);
        }
    }

    public static void bindTexture(Texture texture) {
        if (texture != null) {
            glActiveTexture(GL_TEXTURE0 + texture.num);
            if (texture != state.texture[texture.num]) {
                state.texture[texture.num] = texture;
                glBindTexture(texture.type, texture.id);
            }
        }
    }

    public static void bindTexture(Texture texture, int num) {
        glActiveTexture(GL_TEXTURE0 + num);
        if (texture != state.texture[num]) {
            state.texture[num] = texture;
            if (texture != null) {
                glBindTexture(texture.type, texture.id);
            }
        }
    }

    public static void bindVertexArrayObject(VertexArrayObject vao) {
        if (vao != state.vao) {
            state.vao = vao;
            glBindVertexArray(vao == null ? 0 : vao.id);
        }
    }

    private GLState copy() {
        GLState copy = new GLState();
        copy.blendFunc1 = blendFunc1;
        copy.blendFunc2 = blendFunc2;
        copy.buffers.putAll(buffers);
        copy.flags.putAll(flags);
        copy.framebuffer = framebuffer;
        copy.shader = shader;
        copy.texture = texture;
        copy.vao = vao;
        return copy;
    }

    public static void disable(int... flags) {
        for (int flag : flags) {
            setFlag(flag, false);
        }
    }

    public static void enable(int... flags) {
        for (int flag : flags) {
            setFlag(flag, true);
        }
    }

    public static BufferObject getBuffer(int type) {
        return state.buffers.get(type);
    }

    public static Boolean getFlag(int flag) {
        return state.flags.get(flag);
    }

    public static Framebuffer getFramebuffer() {
        return state.framebuffer;
    }

    public static Shader getShaderProgram() {
        return state.shader;
    }

    public static Texture getTexture(int num) {
        return state.texture[num];
    }

    public static VertexArrayObject getVertexArrayObject() {
        return state.vao;
    }

    public static void setBlendFunc(int blendFunc1, int blendFunc2) {
        if (blendFunc1 != state.blendFunc1 || blendFunc2 != state.blendFunc2) {
            state.blendFunc1 = blendFunc1;
            state.blendFunc2 = blendFunc2;
            glBlendFunc(blendFunc1, blendFunc2);
        }
    }

    public static void setFlag(int flag, boolean value) {
        if (!Objects.equals(state.flags.get(flag), value)) {
            state.flags.put(flag, value);
            if (value) {
                glEnable(flag);
            } else {
                glDisable(flag);
            }
        }
    }

    @Override
    public String toString() {
        return "GLState{" + "blendFunc1=" + blendFunc1 + ", blendFunc2=" + blendFunc2 + ", buffers=" + buffers + ", flags=" + flags + ", framebuffer=" + framebuffer + ", shader=" + shader + ", texture=" + texture + ", vao=" + vao + '}';
    }
}
