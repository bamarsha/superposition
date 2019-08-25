package engine.core

import java.util.*

sealed class Behavior {

    open fun onCreate() {}

    open fun onDestroy() {}

    protected abstract fun <T : Component> require(c: Class<T>): T

    companion object {

        var currentEntity: Entity? = null
    }
}

open class Component : Behavior() {

    private val entity: Entity = currentEntity ?: throw RuntimeException("Components cannot be instantiated directly")

    override fun <T : Component> require(c: Class<T>): T {
        return entity.require(c)
    }
}

open class Entity : Behavior() {

    val behaviors: MutableMap<Class<out Behavior>, Behavior>

    init {
        behaviors = HashMap()
        behaviors[javaClass] = this
    }

    public override fun <T : Component> require(c: Class<T>): T {
        if (behaviors.containsKey(c)) {
            return behaviors[c] as T
        }
        try {
            currentEntity = this
            val newT = c.getConstructor().newInstance()
            currentEntity = null
            behaviors[c] = newT
            return newT
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }
    }
}
