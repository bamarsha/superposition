package engine.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class Behavior {

    private static final Map<Class<? extends Behavior>, Collection<Behavior>> TRACKED_BEHAVIORS = new HashMap();

    public static <T extends Behavior> Collection<T> track(Class<T> c) {
        if (!TRACKED_BEHAVIORS.containsKey(c)) {
            TRACKED_BEHAVIORS.put(c, new HashSet());
        }
        return (Collection) TRACKED_BEHAVIORS.get(c);
    }

    protected void onCreate() {
    }

    protected void onDestroy() {
    }

    protected abstract <T extends Component> T getComponent(Class<T> c);

    public static class Component extends Behavior {

        public final Entity entity;

        public Component(Entity entity) {
            this.entity = entity;
        }

        @Override
        protected final <T extends Component> T getComponent(Class<T> c) {
            return entity.getComponent(c);
        }
    }

    public static class Entity extends Behavior {

        private final Map<Class<? extends Behavior>, Behavior> behaviors;

        public Entity() {
            behaviors = new HashMap();
            behaviors.put(getClass(), this);
        }

        public final void create() {
            for (var entry : behaviors.entrySet()) {
                if (TRACKED_BEHAVIORS.containsKey(entry.getKey())) {
                    TRACKED_BEHAVIORS.get(entry.getKey()).add(entry.getValue());
                }
                entry.getValue().onCreate();
            }
        }

        public final void destroy() {
            for (var entry : behaviors.entrySet()) {
                if (TRACKED_BEHAVIORS.containsKey(entry.getKey())) {
                    TRACKED_BEHAVIORS.get(entry.getKey()).remove(entry.getValue());
                }
                entry.getValue().onDestroy();
            }
        }

        protected final <T extends Component> T addComponent(T c) {
            if (behaviors.containsKey(c.getClass())) {
                throw new IllegalStateException("Component already exists: " + c.getClass());
            }
            behaviors.put(c.getClass(), c);
            return c;
        }

        @Override
        protected final <T extends Component> T getComponent(Class<T> c) {
            if (!behaviors.containsKey(c)) {
                throw new IllegalStateException("Component not found: " + c);
            }
            return (T) behaviors.get(c);
        }
    }
}
