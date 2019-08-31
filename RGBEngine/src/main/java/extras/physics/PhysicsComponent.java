package extras.physics;

import engine.core.Behavior.Component;
import engine.core.Behavior.Entity;
import engine.core.Game;
import engine.util.math.Vec2d;

import java.util.Collection;
import java.util.function.Predicate;

public class PhysicsComponent extends Component<Entity> {

    static {
        Game.declareSystem(PhysicsComponent.class, PhysicsComponent::step);
    }

    public Vec2d position;
    public Vec2d velocity;
    public Predicate<Vec2d> collider;
    public boolean hitWall = false;

    public static Predicate<Vec2d> wallCollider(Vec2d hitboxSize, Collection<Rectangle> walls) {
        return pos -> {
            var hitbox = Rectangle.fromCenterSize(pos, hitboxSize);
            return walls.stream().anyMatch(hitbox::intersects);
        };
    }

    public PhysicsComponent(Entity entity, Vec2d position, Vec2d velocity, Predicate<Vec2d> collider) {
        super(entity);
        this.position = position;
        this.velocity = velocity;
        this.collider = collider;
    }

    private void moveToWall(Vec2d dir) {
        for (var i = 1; i <= 10; i++) {
            var newPos = position.add(dir.mul(Math.pow(.5, i)));
            if (!collider.test(newPos)) {
                position = newPos;
            }
        }
    }

    public void step() {
        var dir = velocity.mul(Game.dt());
        if (hitWall = collider.test(position.add(dir))) {
            moveToWall(new Vec2d(dir.x, 0.0));
            moveToWall(new Vec2d(0.0, dir.y));
        } else {
            position = position.add(dir);
        }
    }
}
