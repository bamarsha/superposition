package engine.core

import java.util.*

sealed class Behavior {

    protected open fun onCreate() {}

    protected open fun onDestroy() {}

    protected abstract fun <T : Component> require(c: Class<T>): T

    open class Component : Behavior() {

        private val entity: Entity

        init {
            if (currentEntity == null) {
                throw RuntimeException("Components cannot be instantiated directly")
            }
            entity = currentEntity!!
            currentEntity = null
        }

        override fun <T : Component> require(c: Class<T>): T {
            return entity.require(c)
        }
    }

    open class Entity : Behavior() {

        private val behaviors: MutableMap<Class<out Behavior>, Behavior>

        init {
            behaviors = HashMap()
            behaviors[javaClass] = this
        }

        fun create() {
            for ((key, value) in behaviors) {
                TRACKED_BEHAVIORS[key]?.add(value)
                value.onCreate()
            }
        }

        fun destroy() {
            for ((key, value) in behaviors) {
                TRACKED_BEHAVIORS[key]?.remove(value)
                value.onDestroy()
            }
        }

        public override fun <T : Component> require(c: Class<T>): T {
            if (behaviors.containsKey(c)) {
                return behaviors[c] as T
            }
            try {
                currentEntity = this
                val newT = c.getConstructor().newInstance()
                behaviors[c] = newT
                return newT
            } catch (ex: Exception) {
                throw RuntimeException(ex)
            }
        }
    }

    companion object {

        var currentEntity: Entity? = null

        private val TRACKED_BEHAVIORS = HashMap<Class<out Behavior>, MutableCollection<Behavior>>()

        fun <T : Behavior> track(c: Class<T>): MutableCollection<T> {
            TRACKED_BEHAVIORS.containsKey(c)
            if (!TRACKED_BEHAVIORS.containsKey(c)) {
                TRACKED_BEHAVIORS[c] = HashSet()
            }
            return (TRACKED_BEHAVIORS[c] ?: throw RuntimeException()) as MutableCollection<T>
        }
    }
}
