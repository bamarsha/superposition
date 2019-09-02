package engine.core;

import engine.graphics.Camera;
import engine.graphics.Camera.Camera2d;
import engine.graphics.Window;
import engine.util.math.Vec2d;

import java.util.BitSet;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public abstract class Input {

    static void init() {
        Window.setCursorPosCallback((window, xpos, ypos) -> {
            mouse = new Vec2d(xpos / Settings.WINDOW_WIDTH, 1 - ypos / Settings.WINDOW_HEIGHT);
        });
        Window.setKeyCallback((window, key, scancode, action, mods) -> {
            if (key >= 0) {
                keys.set(key, action != GLFW_RELEASE);
            }
        });
        Window.setMouseButtonCallback((window, button, action, mods) -> {
            buttons.set(button, action != GLFW_RELEASE);
        });
        Window.setScrollCallback((window, xoffset, yoffset) -> {
            mouseWheel = yoffset;
        });
    }

    static void nextFrame() {
        prevKeys = (BitSet) keys.clone();
        prevMouse = mouse;
        prevButtons = (BitSet) buttons.clone();
        mouseWheel = 0;
    }

    private static BitSet keys = new BitSet();
    private static BitSet prevKeys = new BitSet();

    private static Vec2d mouse = new Vec2d(0, 0);
    private static Vec2d prevMouse = new Vec2d(0, 0);

    private static BitSet buttons = new BitSet();
    private static BitSet prevButtons = new BitSet();

    private static double mouseWheel;

    /**
     * Returns whether the given key is currently being held down
     *
     * @param key The key to check
     * @return Whether the key is currently being held down
     */
    public static boolean keyDown(int key) {
        return keys.get(key);
    }

    /**
     * Returns whether the given key was just pressed down this frame
     * @param key The key to check
     * @return Whether the key was just pressed down this frame
     */
    public static boolean keyJustPressed(int key) {
        return keys.get(key) && !prevKeys.get(key);
    }

    /**
     * Returns whether the given was just released this frame
     * @param key The key to check
     * @return Whether the key was just released this frame
     */
    public static boolean keyJustReleased(int key) {
        return !keys.get(key) && prevKeys.get(key);
    }

    /**
     * Returns the position of the mouse, relative to the default 2d camera
     * @return The position of the mouse
     */
    public static Vec2d mouse() {
        return mouse(Camera.camera2d);
    }

    /**
     * Returns the position of the mouse, relative to the given camera
     * @param camera The camera defining how to transform the mouse
     * @return The position of the mouse
     */
    public static Vec2d mouse(Camera2d camera) {
        return camera.transform(mouse);
    }

    /**
     * Returns how far the mouse moved since the previous frame, relative to the default 2d camera
     * @return The change in position of the mouse
     */
    public static Vec2d mouseDelta() {
        return mouseDelta(Camera.camera2d);
    }

    /**
     * Returns how far the mouse moved since the previous frame, relative ot the given camera
     * @param camera The camera defining how to transform the mouse
     * @return The change in position of the mouse
     */
    public static Vec2d mouseDelta(Camera2d camera) {
        return camera.transform(mouse.sub(prevMouse));
    }

    /**
     * Returns whether the given mouse button is currently being held down
     * @param button The button to check (0 for LMB, 1 for RMB, 2 for middle)
     * @return Whether the button is currently being held down
     */
    public static boolean mouseDown(int button) {
        return buttons.get(button);
    }

    /**
     * Returns whether the given mouse button was just pressed down this frame
     * @param button The button to check (0 for LMB, 1 for RMB, 2 for middle)
     * @return Whether the button was just pressed down this farme
     */
    public static boolean mouseJustPressed(int button) {
        return buttons.get(button) && !prevButtons.get(button);
    }

    /**
     * Returns whether the given mouse button was just released this frame
     * @param button The button to check (0 for LMB, 1 for RMB, 2 for middle)
     * @return Whether the button was just released this frame
     */
    public static boolean mouseJustReleased(int button) {
        return !buttons.get(button) && prevButtons.get(button);
    }

    /**
     * Returns how far the mouse wheel was turned since the previous frame
     * @return How far the mouse wheel was turned since the previous frame
     */
    public static double mouseWheel() {
        return mouseWheel;
    }
}
