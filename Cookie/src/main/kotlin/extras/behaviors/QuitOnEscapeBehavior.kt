package extras.behaviors

import engine.core.Behavior.Entity
import engine.core.Game
import engine.core.Input

import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE

class QuitOnEscapeBehavior : Entity() {

    fun step() {
        if (Input.keyJustPressed(GLFW_KEY_ESCAPE)) {
            Game.stop()
        }
    }

    companion object {

        init {
            Game.declareSystem(QuitOnEscapeBehavior::class.java) { obj: QuitOnEscapeBehavior -> obj.step() }
        }
    }
}
