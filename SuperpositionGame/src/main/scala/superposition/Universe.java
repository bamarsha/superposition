package superposition;

import static engine.core.Game.dt;
import engine.core.Input;
import engine.util.math.Vec2d;

public class Universe {

    public Complex amplitude;
    public GameObjectState[] state;

    public Universe(int numObjects) {
        this.amplitude = new Complex(1.0, 0.0);
        this.state = new GameObjectState[numObjects];
        for (int i = 0; i < state.length; i++) {
            state[i] = new GameObjectState();
            state[i].position = new Vec2d(i + 1, 1);
        }
    }

    public Universe copy() {
        var u = new Universe(state.length);
        u.amplitude = amplitude;
        for (int i = 0; i < state.length; i++) {
            u.state[i] = state[i].copy();
        }
        return u;
    }

    public void physicsStep() {
        for (var s : state) {
            s.position = s.position.add(s.velocity.mul(dt()));
//            if (s.onOff) {
//                s.position = s.position.add(new Vec2d(0, .01 * dt()));
//            }
            s.selected |= Input.mouseDown(s.onOff ? 0 : 0) && Input.mouse().sub(s.position).length() < .5;
            s.selected &= Input.mouseDown(s.onOff ? 0 : 0);
            if (s.selected) {
                s.position = s.position.lerp(Input.mouse(), dt());
            }
        }
    }

    public static class GameObjectState {

        public Vec2d position = new Vec2d(0, 0);
        public Vec2d velocity = new Vec2d(0, 0);
        public boolean onOff;
        public boolean selected;

        public GameObjectState copy() {
            var s = new GameObjectState();
            s.position = position;
            s.velocity = velocity;
            s.onOff = onOff;
            s.selected = selected;
            return s;
        }
    }
}
