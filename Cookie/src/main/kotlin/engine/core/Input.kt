package engine.core

import engine.graphics.Camera
import engine.graphics.Camera.Camera2d
import engine.graphics.Window
import engine.util.Vec2d
import org.lwjgl.glfw.GLFW.GLFW_RELEASE
import java.util.*

object Input {

    private val keys = BitSet()
    private var prevKeys = BitSet()

    private var mouse = Vec2d(0.0, 0.0)
    private var prevMouse = Vec2d(0.0, 0.0)

    private val buttons = BitSet()
    private var prevButtons = BitSet()

    private var mouseWheel: Double = 0.toDouble()

    internal fun init() {
        Window.setCursorPosCallback { window, xpos, ypos -> mouse = Vec2d(xpos / Settings.WINDOW_WIDTH, 1 - ypos / Settings.WINDOW_HEIGHT) }
        Window.setKeyCallback { window, key, scancode, action, mods ->
            if (key >= 0) {
                keys.set(key, action != GLFW_RELEASE)
            }
        }
        Window.setMouseButtonCallback { window, button, action, mods -> buttons.set(button, action != GLFW_RELEASE) }
        Window.setScrollCallback { window, xoffset, yoffset -> mouseWheel = yoffset }
    }

    internal fun nextFrame() {
        prevKeys = keys.clone() as BitSet
        prevMouse = mouse
        prevButtons = buttons.clone() as BitSet
        mouseWheel = 0.0
    }

    fun keyDown(key: Int): Boolean {
        return keys.get(key)
    }

    fun keyJustPressed(key: Int): Boolean {
        return keys.get(key) && !prevKeys.get(key)
    }

    fun keyJustReleased(key: Int): Boolean {
        return !keys.get(key) && prevKeys.get(key)
    }

    @JvmOverloads
    fun mouse(camera: Camera2d = Camera.camera2d): Vec2d {
        return camera.transform(mouse)
    }

    @JvmOverloads
    fun mouseDelta(camera: Camera2d = Camera.camera2d): Vec2d {
        return camera.transform(mouse - prevMouse)
    }

    fun mouseDown(button: Int): Boolean {
        return buttons.get(button)
    }

    fun mouseJustPressed(button: Int): Boolean {
        return buttons.get(button) && !prevButtons.get(button)
    }

    fun mouseJustReleased(button: Int): Boolean {
        return !buttons.get(button) && prevButtons.get(button)
    }

    fun mouseWheel(): Double {
        return mouseWheel
    }
}
