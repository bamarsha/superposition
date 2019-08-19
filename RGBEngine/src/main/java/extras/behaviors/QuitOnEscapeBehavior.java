package extras.behaviors;

import engine.core.Behavior.Entity;
import engine.core.Game;
import engine.core.Input;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

public class QuitOnEscapeBehavior extends Entity {

    static {
        Game.declareSystem(QuitOnEscapeBehavior.class, QuitOnEscapeBehavior::step);
    }

    public void step() {
        if (Input.keyJustPressed(GLFW_KEY_ESCAPE)) {
            Game.stop();
        }
    }
}
