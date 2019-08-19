package extras.behaviors;

import engine.core.Behavior.Entity;
import engine.core.Game;
import static engine.core.Game.dt;
import engine.graphics.Window;
import java.util.LinkedList;
import java.util.Queue;

public class FPSBehavior extends Entity {

    static {
        Game.declareSystem(FPSBehavior.class, FPSBehavior::step);
    }

    private final Queue<Double> tList = new LinkedList();
    public double fps;
    private double timeElapsed;

    public void step() {
        double t = System.nanoTime() / 1e9;
        tList.add(t);
        while (t - tList.peek() > 5) {
            tList.poll();
        }
        fps = tList.size() / 5;

        timeElapsed += dt();
        if (timeElapsed > .25) {
            timeElapsed -= .25;
            Window.setTitle("FPS: " + Math.round(fps));
        }
    }
}
