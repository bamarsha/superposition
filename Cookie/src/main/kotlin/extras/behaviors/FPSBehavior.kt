package extras.behaviors

import engine.core.Entity
import engine.core.Game
import engine.core.Game.dt
import engine.graphics.Window
import java.util.*

class FPSBehavior : Entity() {

    private val tList = LinkedList<Double>()
    var fps: Double = 0.toDouble()
    private var timeElapsed: Double = 0.toDouble()

    fun step() {
        val t = System.nanoTime() / 1e9
        tList.add(t)
        while (t - tList.peek() > 5) {
            tList.poll()
        }
        fps = (tList.size / 5).toDouble()

        timeElapsed += dt()
        if (timeElapsed > .25) {
            timeElapsed -= .25
            Window.setTitle("FPS: " + Math.round(fps))
        }
    }

    companion object {

        init {
            Game.declareSystem(FPSBehavior::class.java) { obj -> obj.step() }
        }
    }
}
