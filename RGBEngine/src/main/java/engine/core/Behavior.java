package engine.core;

import java.util.HashMap;
import java.util.Map;

public abstract class Behavior {

    public abstract <T extends Component> T get(Class<T> c);

    protected void onCreate() {
    }

    protected void onDestroy() {
    }

    public static class Component<E extends Entity> extends Behavior {

        public final E entity;

        public Component(E entity) {
            this.entity = entity;
        }

        @Override
        public final <T extends Component> T get(Class<T> c) {
            return entity.get(c);
        }
    }

    public static class Entity extends Behavior {

        final Map<Class<? extends Behavior>, Behavior> behaviors;

        public Entity() {
            behaviors = new HashMap<>();
            behaviors.put(getClass(), this);
        }

        protected final void addAll(Component... a) {
            for (var c : a) {
                add(c);
            }
        }

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

        public final boolean has(Class<? extends Component> c) {
            return behaviors.containsKey(c);
        }
    }
}
