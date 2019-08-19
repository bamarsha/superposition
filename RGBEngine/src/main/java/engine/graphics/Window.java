package engine.graphics;

import engine.core.Settings;
import engine.graphics.opengl.Framebuffer;
import java.nio.IntBuffer;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.*;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.glViewport;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryUtil.NULL;

public abstract class Window {

    private static long handle;

    public static void cleanupGLFW() {
        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);
        glfwTerminate();
        if (Settings.SHOW_OPENGL_DEBUG_INFO) {
            glfwSetErrorCallback(null).free();
        }
    }

    public static void init() {
        if (Settings.SHOW_OPENGL_DEBUG_INFO) {
            GLFWErrorCallback.createThrow().set();
            Configuration.DEBUG.set(true);
        }

        if (!glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW");
        }
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        if (Settings.RESIZEABLE_WINDOW) {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        }
        if (Settings.ANTI_ALIASING > 1) {
            glfwWindowHint(GLFW_SAMPLES, Settings.ANTI_ALIASING);
        }

        handle = glfwCreateWindow(Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT, "Hello World!", NULL, NULL);
        if (handle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        try ( MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(handle, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(handle, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
        }

        setCursorEnabled(Settings.SHOW_CURSOR);

        glfwMakeContextCurrent(handle);
        GL.createCapabilities();
        glfwSwapInterval(0);

//        GLState.enable(GL_DEPTH_TEST, GL_BLEND);
//        GLState.setBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        if (Settings.SHOW_OPENGL_DEBUG_INFO) {
            GLUtil.setupDebugMessageCallback();
        }

        if (Settings.ENABLE_VSYNC) {
            glfwSwapInterval(1);
        }

        glfwSetFramebufferSizeCallback(handle, (window, width, height) -> {
            Settings.WINDOW_WIDTH = width;
            Settings.WINDOW_HEIGHT = height;
            glViewport(0, 0, Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT);
        });

        glfwShowWindow(handle);
    }

    public static void nextFrame() {
        glfwSwapBuffers(handle);
        glfwPollEvents();
        Framebuffer.clearWindow(Settings.BACKGROUND_COLOR);
    }

    public static void resizeWindow(int width, int height) {
        glfwSetWindowSize(handle, width, height);
    }

    public static void setCursorEnabled(boolean enabled) {
        if (enabled) {
            glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        } else {
            glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        }
    }

    public static void setCursorPosCallback(GLFWCursorPosCallbackI cursorPosCallback) {
        glfwSetCursorPosCallback(handle, cursorPosCallback);
    }

    public static void setKeyCallback(GLFWKeyCallbackI keyCallback) {
        glfwSetKeyCallback(handle, keyCallback);
    }

    public static void setMouseButtonCallback(GLFWMouseButtonCallbackI mouseButtonCallback) {
        glfwSetMouseButtonCallback(handle, mouseButtonCallback);
    }

    public static void setScrollCallback(GLFWScrollCallbackI scrollCallback) {
        glfwSetScrollCallback(handle, scrollCallback);
    }

    public static void setTitle(String s) {
        glfwSetWindowTitle(handle, s);
    }

    public static boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }
}
