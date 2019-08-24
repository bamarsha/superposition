package engine.core

import engine.core.Behavior.Companion.track
import engine.graphics.Window
import java.util.*


object Game {

    private val SYSTEMS = LinkedList<() -> Unit>()

    private var prevTime: Long = 0
    private var dt: Double = 0.toDouble()
    private var shouldClose: Boolean = false

    fun declareSystem(r: () -> Unit) {
        SYSTEMS.add(r)
    }

    fun <T : Behavior> declareSystem(c: Class<T>, func: (T) -> Unit) {
        val myBehaviors = track(c)
        declareSystem {
            for (b in myBehaviors) {
                func(b)
            }
        }
    }

    fun dt(): Double {
        return dt
    }

    fun init() {
        Window.init()
        Input.init()
    }

    fun run() {
        while (!shouldClose && !Window.shouldClose()) {
            Input.nextFrame()
            Window.nextFrame()

            var time = System.nanoTime()
            dt = Math.min((time - prevTime) / 1e9, Settings.MAX_FRAME_TIME)
            while (dt < Settings.MIN_FRAME_TIME) {
                try {
                    Thread.sleep(0, 100)
                } catch (ex: InterruptedException) {
                    throw RuntimeException(ex)
                }

                time = System.nanoTime()
                dt = Math.min((time - prevTime) / 1e9, Settings.MAX_FRAME_TIME)
            }
            prevTime = time

            for (s in SYSTEMS) {
                s()
            }
        }
        Window.cleanupGLFW()
        System.exit(0)
    }

    fun stop() {
        shouldClose = true
    }
}
