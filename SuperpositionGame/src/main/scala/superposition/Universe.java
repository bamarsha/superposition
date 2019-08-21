package superposition;

import static engine.core.Game.dt;
import engine.core.Input;
import engine.util.math.Vec2d;

public class Universe {

    public final GameLevel gameLevel;
    public Complex amplitude;
    public GameObjectState[] state;

    public Universe(GameLevel gameLevel) {
        this.gameLevel = gameLevel;
        this.amplitude = new Complex(1);
        this.state = new GameObjectState[GameLevel.NumObjects()];
        for (int i = 0; i < GameLevel.NumObjects(); i++) {
            state[i] = new GameObjectState();
            state[i].position = new Vec2d(i + 1, 1);
        }
    }

    public Universe copy() {
        var u = new Universe(gameLevel);
        u.amplitude = amplitude;
        for (int i = 0; i < GameLevel.NumObjects(); i++) {
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
