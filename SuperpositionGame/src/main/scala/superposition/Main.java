package superposition;

import engine.core.Game;
import engine.core.Settings;
import engine.graphics.Camera;
import engine.util.Color;
import engine.util.math.Vec2d;
import extras.behaviors.FPSBehavior;
import extras.behaviors.QuitOnEscapeBehavior;

public class Main {

    public static void main(String[] args) {
        Settings.BACKGROUND_COLOR = Color.GRAY;
        Game.init();

        Camera.camera2d.upperRight = new Vec2d(16, 9);

        new FPSBehavior().create();
        new QuitOnEscapeBehavior().create();
        new GameLevel().create();

        Game.run();
    }
}
