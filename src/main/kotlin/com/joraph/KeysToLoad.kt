package com.joraph

import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Simple class for managing the keys that need to be loaded and those
 * that have already been loaded.
 */
class KeysToLoad {

    private val keysToLoad: MutableMap<Class<*>, MutableSet<Any>> = mutableMapOf()
    private val keysLoaded: MutableMap<Class<*>, MutableSet<Any>> = mutableMapOf()

    fun getAddKeyToLoadFunction(entityClass: Class<*>): (Any) -> Unit = { addKeyToLoad(entityClass, it) }

    @Synchronized
    fun addKeyToLoad(entityClass: Class<*>, id: Any) {
        if (id !in getKeysLoaded(entityClass)) {
            getKeysToLoad(entityClass).add(id)
        }
    }

    @Synchronized
    fun addKeysLoaded(entityClass: Class<*>, ids: Collection<Any>) {
        getKeysLoaded(entityClass).addAll(ids)
        getKeysToLoad(entityClass).removeAll(ids)
    }

    fun getKeysToLoad(entityClass: Class<*>): MutableSet<Any> {
        return keysToLoad.computeIfAbsent(entityClass) { Collections.newSetFromMap(ConcurrentHashMap()) }
    }

    fun getKeysLoaded(entityClass: Class<*>): MutableSet<Any> {
        return keysLoaded.computeIfAbsent(entityClass) { Collections.newSetFromMap(ConcurrentHashMap()) }
    }

    @Synchronized
    fun clear() {
        keysToLoad.clear()
        keysLoaded.clear()
    }

    @get:Synchronized
    val entitiesToLoad: Set<Class<*>>
        get() = keysToLoad.entries
                .filter{ !it.value.isEmpty() }
                .map{ it.key }
                .toSet()
}
