package engine.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents any object or data that exists in the game world.
 */
public abstract class Behavior {

    /**
     * Gets the component of the given type associated with this behavior.
     * Throws an IllegalArgumentException if the component doesn't exist.
     *
     * @param c The class of the component to get
     * @return The component
     */
    public abstract <T extends Component> T get(Class<T> c);

    /**
     * Is called when this behavior is created
     */
    protected void onCreate() {
    }

    /**
     * Is called when this behavior is destroyed
     */
    protected void onDestroy() {
    }

    /**
     * Represents any data that exists in the game world
     */
    public static class Component<E extends Entity> extends Behavior {

        /**
         * The entity that has this component
         */
        public final E entity;

        /**
         * Constructs a new component with the given entity
         *
         * @param entity The entity that owns this component
         */
        public Component(E entity) {
            this.entity = entity;
        }

        @Override
        public final <T extends Component> T get(Class<T> c) {
            return entity.get(c);
        }
    }

    /**
     * Represents any object that exists in the game world
     */
    public static class Entity extends Behavior {

        /**
         * The behaviors that belong to this entity
         */
        final Map<Class<? extends Behavior>, Behavior> behaviors;

        /**
         * Stores whether this entity is currently active in the game world
         */
        boolean created = false;

        /**
         * Constructs a new entity
         */
        public Entity() {
            behaviors = new HashMap<>();
            behaviors.put(getClass(), this);
        }

        /**
         * Adds all the given components to this entity
         * @param a The array of components to add
         */
        protected final void addAll(Component... a) {
            for (var c : a) {
                add(c);
            }
        }

        /**
         * Adds the given component to this entity.
         * Throws an IllegalStateException if a component of the same type has already been added to this entity.
         * @param c The component to add
         * @return The added component
         */
        protected final <T extends Component> T add(T c) {
            if (behaviors.containsKey(c.getClass())) {
                throw new IllegalStateException("Component already exists: " + c.getClass());
            }
            behaviors.put(c.getClass(), c);
            return c;
        }

        @SuppressWarnings("unchecked")
        @Override
        public final <T extends Component> T get(Class<T> c) {
            if (!behaviors.containsKey(c)) {
                throw new IllegalArgumentException("Component not found: " + c);
            }
            return (T) behaviors.get(c);
        }

        /**
         * Returns whether this entity has a component of the given type
         * @param c The class of the component to check for
         * @return Whether this entity has a component of the given type
         */
        public final boolean has(Class<? extends Component> c) {
            return behaviors.containsKey(c);
        }

        /**
         * Returns whether this entity is currently active in the game world
         *
         * @return Whether this entity is currently active in the game world
         */
        public final boolean isCreated() {
            return created;
        }
    }
}
