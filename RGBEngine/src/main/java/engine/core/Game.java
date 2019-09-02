package engine.core;

import engine.graphics.Window;

import java.util.*;
import java.util.function.Consumer;

import static engine.core.Behavior.Entity;

public abstract class Game {

    private static final Map<Class<? extends Behavior>, Collection<Behavior>> BEHAVIORS = new HashMap<>();
    private static final List<Runnable> SYSTEMS = new LinkedList<>();

    /**
     * Declares that the given code should be run every frame
     *
     * @param r The code to run every frame
     */
    public static void declareSystem(Runnable r) {
        SYSTEMS.add(r);
    }

    /**
     * Declares that the given code should be run every frame on each instance of the given behavior
     * that currently exists in the game world
     * @param c The type of behavior to run the code on
     * @param func The code to run on each behavior
     */
    public static <T extends Behavior> void declareSystem(Class<T> c, Consumer<T> func) {
        var myBehaviors = track(c);
        declareSystem(() -> {
            for (var b : myBehaviors) {
                func.accept(b);
            }
        });
    }

    /**
     * Adds the given entity to the game world.
     * Throws an IllegalStateException if the entity has already been created.
     * @param e The entity to create
     */
    public static void create(Entity e) {
        if (e.created) {
            throw new IllegalStateException("Behavior was already created");
        }
        e.created = true;
        for (var entry : e.behaviors.entrySet()) {
            trackUntyped(entry.getKey()).add(entry.getValue());
            entry.getValue().onCreate();
        }
    }

    /**
     * Removes the given entity from the game world.
     * Throws an IllegalStateException if the entity has already been destroyed.
     * @param e The entity to destroy
     */
    public static void destroy(Entity e) {
        if (!e.created) {
            throw new IllegalStateException("Behavior was already destroyed");
        }
        e.created = false;
        for (var entry : e.behaviors.entrySet()) {
            trackUntyped(entry.getKey()).remove(entry.getValue());
            entry.getValue().onDestroy();
        }
    }

    /**
     * Returns a collection that stores all instances of the given behavior that currently exist in the game world.
     * The returned collection is automatically mutated as behaviors are created or destroyed in the future.
     *
     * @param c The class of the behavior to track
     * @return The collection of created behaviors
     */
    @SuppressWarnings("unchecked")
    public static <T extends Behavior> Collection<T> track(Class<T> c) {
        return (Collection<T>) trackUntyped(c);
    }

    private static Collection<Behavior> trackUntyped(Class<? extends Behavior> c) {
        if (!BEHAVIORS.containsKey(c)) {
            BEHAVIORS.put(c, new HashSet<>());
        }
        return BEHAVIORS.get(c);
    }

    private static long prevTime;
    private static double dt;
    private static boolean shouldClose;

    /**
     * Returns the amount of time in seconds that has passed since the last frame
     * @return The amount of time in seconds that has passed since the last frame
     */
    public static double dt() {
        return dt;
    }

    /**
     * Initializes the game. Should be called exactly once, at the very start of the main method.
     */
    public static void init() {
        Window.init();
        Input.init();
    }

    /**
     * Runs the game. Should be called exactly one, at the very end of the main method.
     */
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

    /**
     * Tells the game to stop. The game will quit at the end of the current frame.
     */
    public static void stop() {
        shouldClose = true;
    }
}
