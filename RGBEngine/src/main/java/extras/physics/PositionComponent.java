package extras.physics;

import engine.core.Behavior.Component;
import engine.core.Behavior.Entity;
import engine.util.math.Vec2d;

/**
 * Contains the position of an entity.
 */
public class PositionComponent extends Component<Entity> {
    /**
     * The value of the position.
     */
    public Vec2d value;

    /**
     * Constructs a new position component.
     *
     * @param entity the entity that owns this component
     * @param value  the value of the position
     */
    public PositionComponent(Entity entity, Vec2d value) {
        super(entity);
        this.value = value;
    }
}
