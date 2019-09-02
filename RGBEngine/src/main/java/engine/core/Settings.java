package engine.core;

import engine.util.Color;

public abstract class Settings {

    /**
     * The default width in pixels of the window
     */
    public static int WINDOW_WIDTH = 1600;
    /**
     * The default height in pixels of the window
     */
    public static int WINDOW_HEIGHT = 900;

    /**
     * The background color to fill the window with each frame
     */
    public static Color BACKGROUND_COLOR = Color.BLACK;
    /**
     * How many samples we should use for multi-sample anti-aliasing.
     * This number should be a power of 2, between 1 (off) to 16 (max).
     */
    public static int ANTI_ALIASING = 1;
    /**
     * Whether we should enable vertical synchronization for the window
     */
    public static boolean ENABLE_VSYNC = true;
    /**
     * Whether the user can resize the window
     */
    public static boolean RESIZEABLE_WINDOW = true;
    /**
     * Whether we show the mouse cursor over the window
     */
    public static boolean SHOW_CURSOR = true;

    /**
     * Whether we should print OpenGL debug information
     */
    public static boolean SHOW_OPENGL_DEBUG_INFO = true;

    /**
     * The minimum time in seconds allowed between frames
     */
    public static double MIN_FRAME_TIME = .001;
    /**
     * The maximum time in seconds allowed between frames
     */
    public static double MAX_FRAME_TIME = .1;
}
