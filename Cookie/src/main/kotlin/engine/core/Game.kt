package engine.core

import engine.graphics.Window
import java.util.*


object Game {

    private val TRACKED_BEHAVIORS = HashMap<Class<out Behavior>, MutableCollection<Behavior>>()
    private val SYSTEMS = LinkedList<() -> Unit>()

    private var prevTime: Long = 0
    private var dt: Double = 0.toDouble()
    private var shouldClose: Boolean = false

    fun create(e: Entity) {
        for ((key, value) in e.behaviors) {
            track(key)
            TRACKED_BEHAVIORS[key]!!.add(value)
            value.onCreate()
        }
    }

    fun destroy(e: Entity) {
        for ((key, value) in e.behaviors) {
            track(key).remove(value)
            value.onDestroy()
        }
    }

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

    fun <T : Behavior> track(c: Class<T>): MutableCollection<T> {
        if (!TRACKED_BEHAVIORS.containsKey(c)) {
            TRACKED_BEHAVIORS[c] = HashSet()
        }
        return (TRACKED_BEHAVIORS[c] ?: throw RuntimeException()) as MutableCollection<T>
    }
}
