package engine.graphics

import engine.core.Settings
import engine.graphics.opengl.Framebuffer
import engine.graphics.opengl.GLState
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GLUtil
import org.lwjgl.system.Configuration
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.NULL


object Window {

    private var handle: Long = 0

    fun cleanupGLFW() {
        glfwFreeCallbacks(handle)
        glfwDestroyWindow(handle)
        glfwTerminate()
        if (Settings.SHOW_OPENGL_DEBUG_INFO) {
            glfwSetErrorCallback(null)!!.free()
        }
    }

    fun init() {
        if (Settings.SHOW_OPENGL_DEBUG_INFO) {
            GLFWErrorCallback.createThrow().set()
            Configuration.DEBUG.set(true)
        }

        if (!glfwInit()) {
            throw RuntimeException("Failed to initialize GLFW")
        }
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        if (Settings.RESIZEABLE_WINDOW) {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
        }
        if (Settings.ANTI_ALIASING > 1) {
            glfwWindowHint(GLFW_SAMPLES, Settings.ANTI_ALIASING)
        }

        handle = glfwCreateWindow(Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT, "Hello World!", NULL, NULL)
        if (handle == NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)
            glfwGetWindowSize(handle, pWidth, pHeight)
            val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())
            glfwSetWindowPos(handle, (vidmode!!.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2)
        }

        setCursorEnabled(Settings.SHOW_CURSOR)

        glfwMakeContextCurrent(handle)
        GL.createCapabilities()
        glfwSwapInterval(0)

        GLState.enable(GL_BLEND)
        GLState.setBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        if (Settings.SHOW_OPENGL_DEBUG_INFO) {
            GLUtil.setupDebugMessageCallback()
        }

        if (Settings.ENABLE_VSYNC) {
            glfwSwapInterval(1)
        }

        glfwSetFramebufferSizeCallback(handle) { window, width, height ->
            Settings.WINDOW_WIDTH = width
            Settings.WINDOW_HEIGHT = height
            glViewport(0, 0, Settings.WINDOW_WIDTH, Settings.WINDOW_HEIGHT)
        }

        glfwShowWindow(handle)
    }

    fun nextFrame() {
        glfwSwapBuffers(handle)
        glfwPollEvents()
        Framebuffer.clearWindow(Settings.BACKGROUND_COLOR)
    }

    fun resizeWindow(width: Int, height: Int) {
        glfwSetWindowSize(handle, width, height)
    }

    fun setCursorEnabled(enabled: Boolean) {
        if (enabled) {
            glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
        } else {
            glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
        }
    }

    fun setCursorPosCallback(cursorPosCallback: (Long, Double, Double) -> Unit) {
        glfwSetCursorPosCallback(handle, cursorPosCallback)
    }

    fun setKeyCallback(keyCallback: (Long, Int, Int, Int, Int) -> Unit) {
        glfwSetKeyCallback(handle, keyCallback)
    }

    fun setMouseButtonCallback(mouseButtonCallback: (Long, Int, Int, Int) -> Unit) {
        glfwSetMouseButtonCallback(handle, mouseButtonCallback)
    }

    fun setScrollCallback(scrollCallback: (Long, Double, Double) -> Unit) {
        glfwSetScrollCallback(handle, scrollCallback)
    }

    fun setTitle(s: String) {
        glfwSetWindowTitle(handle, s)
    }

    fun shouldClose(): Boolean {
        return glfwWindowShouldClose(handle)
    }
}
