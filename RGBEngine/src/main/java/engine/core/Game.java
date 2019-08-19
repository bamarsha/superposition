package engine.core;

import static engine.core.Behavior.track;
import engine.graphics.Window;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public abstract class Game {

    private static final List<Runnable> SYSTEMS = new LinkedList();

    public static void declareSystem(Runnable r) {
        SYSTEMS.add(r);
    }

    public static <T extends Behavior> void declareSystem(Class<T> c, Consumer<T> func) {
        var myBehaviors = track(c);
        declareSystem(() -> {
            for (var b : myBehaviors) {
                func.accept((T) b);
            }
        });
    }

    private static long prevTime;
    private static double dt;
    private static boolean shouldClose;

    public static double dt() {
        return dt;
    }

    public static void init() {
        Window.init();
        Input.init();
    }

    public static void run() {
        while (!shouldClose && !Window.shouldClose()) {
            Input.nextFrame();
            Window.nextFrame();

            long time = System.nanoTime();
            dt = Math.min((time - prevTime) / 1e9, Settings.MAX_FRAME_TIME);
            while (dt < Settings.MIN_FRAME_TIME) {
                try {
                    Thread.sleep(0, 100);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                time = System.nanoTime();
                dt = Math.min((time - prevTime) / 1e9, Settings.MAX_FRAME_TIME);
            }
            prevTime = time;

            for (var s : SYSTEMS) {
                s.run();
            }
        }
        Window.cleanupGLFW();
        System.exit(0);
    }

    public static void stop() {
        shouldClose = true;
    }
}
