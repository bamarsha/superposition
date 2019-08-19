package superposition;

import engine.core.Behavior.Entity;
import static engine.core.Game.declareSystem;
import static engine.core.Game.dt;
import engine.core.Input;
import engine.graphics.Camera;
import engine.graphics.Camera.Camera2d;
import engine.graphics.opengl.Framebuffer;
import engine.graphics.opengl.Shader;
import engine.graphics.opengl.Texture;
import engine.graphics.sprites.Sprite;
import static engine.util.Color.*;
import engine.util.math.Transformation;
import engine.util.math.Vec2d;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import static org.lwjgl.glfw.GLFW.*;

public class GameLevel extends Entity {

    static {
        declareSystem(GameLevel.class, GameLevel::step);
    }

    public int numObjects = 2;
    public List<Universe> universes;

    public void applyGate(String gate, int target, int... controls) {
        var newUniverses = new LinkedList();
        for (var u : universes) {
            boolean allMatch = true;
            for (var c : controls) {
                if (!u.state[c].onOff) {
                    allMatch = false;
                }
            }
            if (allMatch) {
                switch (gate) {
                    case "X":
                        u.state[target].onOff = !u.state[target].onOff;
                        break;
                    case "Z":
                        if (u.state[target].onOff) {
                            u.amplitude = u.amplitude.mul(new Complex(-1, 0));
                        }
                        break;
                    case "T":
                        if (u.state[target].onOff) {
                            u.amplitude = u.amplitude.mul(Complex.polar(1, Math.PI / 4));
                        }
                        break;
                    case "H":
                        u.amplitude = u.amplitude.mul(new Complex(Math.sqrt(.5)));
                        var phase = u.state[target].onOff ? -1 : 1;
                        var newU = u.copy();
                        u.amplitude = u.amplitude.mul(new Complex(phase));
                        newU.state[target].onOff = !newU.state[target].onOff;
                        newUniverses.add(newU);
                        break;
                }
            }
        }
        universes.addAll(newUniverses);
    }

    @Override
    public void onCreate() {
        universes = new LinkedList();
        universes.add(new Universe(this));
        framebuffer = new Framebuffer();
        colorBuffer = framebuffer.attachColorBuffer();
    }

    public boolean similar(Universe u1, Universe u2) {
        for (int i = 0; i < numObjects; i++) {
            var s1 = u1.state[i];
            var s2 = u2.state[i];
            if (s1.onOff != s2.onOff) {
                return false;
            }
//            if (s1.position.sub(s2.position).length() > .5) {
//                return false;
//            }
//            if (s1.velocity.sub(s2.velocity).length() > .5) {
//                return false;
//            }
        }
        return true;
    }

    public void simplify() {
        var newUniverses = new LinkedList<Universe>();
        for (var u : universes) {
            var anyMatch = false;
            for (var u2 : newUniverses) {
                if (similar(u, u2)) {
                    anyMatch = true;
                    u2.amplitude = u2.amplitude.add(u.amplitude);
                    break;
                }
            }
            if (!anyMatch) {
                newUniverses.add(u);
            }
        }
        universes = new LinkedList();
        double sum = 0;
        for (var u : newUniverses) {
            if (u.amplitude.magnitudeSquared() > 1e-6) {
                universes.add(u);
                sum += u.amplitude.magnitudeSquared();
            }
        }
        for (var u : universes) {
            u.amplitude = u.amplitude.div(new Complex(Math.sqrt(sum)));
        }
    }

    public void step() {
        var hover = new HashSet<Integer>();
        for (var u : universes) {
            for (int i = 0; i < numObjects; i++) {
                if (u.state[i].position.sub(Input.mouse()).length() < .5) {
                    hover.add(i);
                }
            }
        }
        for (var i : hover) {
            if (Input.keyJustPressed(GLFW_KEY_X)) {
                applyGate("X", i);
            }
            if (Input.keyJustPressed(GLFW_KEY_Z)) {
                applyGate("Z", i);
            }
            if (Input.keyJustPressed(GLFW_KEY_T)) {
                applyGate("T", i);
            }
            if (Input.keyJustPressed(GLFW_KEY_H)) {
                applyGate("H", i);
            }
        }

        for (var u : universes) {
            u.physicsStep();
        }
        simplify();
        draw();
    }

    private Framebuffer framebuffer;
    private Texture colorBuffer;
    private static final Shader shader = Shader.load("universe");
    private double time = 0;

    public void draw() {
        time += dt();
        shader.setUniform("time", (float) time);
        double minVal = 0;
        for (var u : universes) {
            double maxVal = minVal + u.amplitude.magnitudeSquared();

            framebuffer.clear(CLEAR);
            for (var s : u.state) {
                var color = s.onOff ? WHITE : BLACK;
                Sprite.load("cat.png").draw(Transformation.create(s.position, 0, 1), color);
            }
            var cam = new Camera2d();
            cam.lowerLeft = new Vec2d(-1, -1);
            Camera.current = cam;
            shader.setMVP(Transformation.IDENTITY);
            shader.setUniform("minVal", (float) minVal);
            shader.setUniform("maxVal", (float) maxVal);
            shader.setUniform("hue", (float) (u.amplitude.phase() / (2 * Math.PI)));
            Framebuffer.drawToWindow(colorBuffer, shader);
            Camera.current = Camera.camera2d;

            minVal = maxVal;
        }
    }
}
