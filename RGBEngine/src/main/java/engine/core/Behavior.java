package engine.core;

import java.lang.reflect.InvocationTargetException;
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

    protected abstract <T extends Component> T require(Class<T> c);

    public static class Component extends Behavior {

        private static Entity currentEntity;
        private final Entity entity;

        public Component() {
            if (currentEntity == null) {
                throw new RuntimeException("Components cannot be instantiated directly");
            }
            entity = currentEntity;
            currentEntity = null;
        }

        @Override
        protected final <T extends Component> T require(Class<T> c) {
            return entity.require(c);
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
                    TRACKED_BEHAVIORS.remove(entry.getKey()).add(entry.getValue());
                }
                entry.getValue().onDestroy();
            }
        }

        @Override
        protected final <T extends Component> T require(Class<T> c) {
            if (behaviors.containsKey(c)) {
                return (T) behaviors.get(c);
            }
            try {
                Component.currentEntity = this;
                T newT = c.getConstructor().newInstance();
                behaviors.put(c, newT);
                return newT;
            } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
