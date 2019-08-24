package engine.core

import engine.util.Color

object Settings {

    var WINDOW_WIDTH = 1600
    var WINDOW_HEIGHT = 900

    var BACKGROUND_COLOR = Color.BLACK
    var ANTI_ALIASING = 1 // Scale from 1 - 16
    var ENABLE_VSYNC = true
    var RESIZEABLE_WINDOW = true
    var SHOW_CURSOR = true

    var SHOW_OPENGL_DEBUG_INFO = true
    var MULTITHREADED_OPENGL = false

    var MIN_FRAME_TIME = .001
    var MAX_FRAME_TIME = .1
}
