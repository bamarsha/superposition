package extras.examples;

import extras.behaviors.FPSBehavior;
import extras.behaviors.QuitOnEscapeBehavior;
import engine.core.Game;
import static engine.core.Game.dt;
import engine.core.Input;
import engine.core.Settings;
import engine.graphics.Camera;
import engine.graphics.Graphics;
import engine.graphics.Window;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_H;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import engine.util.Color;
import static engine.util.math.MathUtils.clamp;
import static engine.util.math.MathUtils.floor;
import static engine.util.math.MathUtils.mod;
import engine.util.math.Transformation;
import engine.util.math.Vec2d;

public class GameOfLife {

    private static final int SIZE = 1000;
    private static boolean[][] STATE = new boolean[SIZE][SIZE];

    private static Vec2d viewPos = new Vec2d(0, 0);
    private static double viewZoom = 0;
    private static Vec2d viewSize = new Vec2d(16, 9);
    private static boolean running = false;

    public static void main(String[] args) {
        Game.init();

        new FPSBehavior().create();
        new QuitOnEscapeBehavior().create();

        Game.declareSystem(() -> {
            double dx = 0, dy = 0;

            if (Input.keyDown(GLFW_KEY_W)) {
                dy += 1;
            }
            if (Input.keyDown(GLFW_KEY_A)) {
                dx -= 1;
            }
            if (Input.keyDown(GLFW_KEY_S)) {
                dy -= 1;
            }
            if (Input.keyDown(GLFW_KEY_D)) {
                dx += 1;
            }
            viewPos = viewPos.add(new Vec2d(dx, dy).mul(5 * Math.pow(2, -.5 * viewZoom) * dt()));

            viewZoom += Input.mouseWheel();
            viewZoom = clamp(viewZoom, -14, 0);

            if (Input.mouseDown(0)) {
                set(floor(Input.mouse().x), floor(Input.mouse().y), true);
            }
            if (Input.mouseDown(1)) {
                set(floor(Input.mouse().x), floor(Input.mouse().y), false);
            }
            if (Input.keyJustPressed(GLFW_KEY_SPACE)) {
                running = !running;
            }
            if (Input.keyJustPressed(GLFW_KEY_R)) {
                for (int x = 0; x < SIZE; x++) {
                    for (int y = 0; y < SIZE; y++) {
                        if (Math.random() < .01) {
                            set(x, y, !get(x, y));
                        }
                    }
                }
            }

            if (running) {
                STATE = nextState();
            }
            if (Input.keyJustPressed(GLFW_KEY_H)) {
                Window.resizeWindow(400, 400);
            }

            viewSize = new Vec2d(16, 16.0 * Settings.WINDOW_HEIGHT / Settings.WINDOW_WIDTH);
            Camera.camera2d.setCenterSize(viewPos, viewSize.mul(Math.pow(2, -.5 * viewZoom)));
            for (int x = floor(Camera.camera2d.lowerLeft.x); x < Camera.camera2d.upperRight.x; x++) {
                for (int y = floor(Camera.camera2d.lowerLeft.y); y < Camera.camera2d.upperRight.y; y++) {
                    if (get(x, y)) {
                        Graphics.drawRectangle(Transformation.create(new Vec2d(x, y), 0, 1), Color.WHITE);
                    }
                }
            }
        });

        Game.run();
    }

    private static boolean get(int x, int y) {
        return STATE[mod(x, SIZE)][mod(y, SIZE)];
    }

    private static boolean[][] nextState() {
        boolean[][] nextState = new boolean[SIZE][SIZE];
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                int neighborCount = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        neighborCount += get(x + i, y + j) ? 1 : 0;
                    }
                }
                nextState[x][y] = neighborCount == 3 || (neighborCount == 4 && get(x, y));
            }
        }
        return nextState;
    }

    private static void set(int x, int y, boolean val) {
        STATE[mod(x, SIZE)][mod(y, SIZE)] = val;
    }
}
