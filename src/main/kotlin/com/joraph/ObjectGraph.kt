package com.joraph

import com.joraph.schema.Property
import com.joraph.schema.Schema
import java.util.function.Predicate
import java.util.stream.Stream

class ObjectGraph @JvmOverloads constructor(private val schema: Schema? = null) :
        Cloneable,
        Iterable<Pair<Class<*>, MutableMap<Any, Any>>> {

    private val results: MutableMap<Class<*>, MutableMap<Any, Any>> = mutableMapOf()

    @Throws(CloneNotSupportedException::class)
    override fun clone(): ObjectGraph {
        val ret = ObjectGraph(schema)
        this.copyGraphTo(ret)
        return ret
    }

    override fun iterator(): Iterator<Pair<Class<*>, MutableMap<Any, Any>>> = results.entries
            .map { Pair(it.key, it.value) }
            .iterator()

    /**
     * Returns the graph type key for the given entity class.
     */
    fun getGraphTypeKey(entityClass: Class<*>): Class<*> {
        return if (schema != null) schema.getGraphTypeKey(entityClass) else entityClass
    }

    /**
     * Copy this [ObjectGraph]'s results to the given [ObjectGraph].
     */
    fun copyGraphTo(destinationObjectGraph: ObjectGraph) {
        for ((key, value) in this) {
            value.putAll(destinationObjectGraph.results.computeIfAbsent(key) { mutableMapOf() })
        }
    }

    /**
     * Copy the given [ObjectGraph]'s results to the this [ObjectGraph].
     */
    fun copyGraphFrom(objectGraph: ObjectGraph) = objectGraph.copyGraphTo(this)

    /**
     * Returns all of the ids for the given type.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getIds(type: Class<*>): Set<T> {
        return schema!!.getEntityDescriptors(type)
                .map { getList<T>(it.entityClass)
                        .map { o -> it.primaryKey.read(o) as T } }
                .flatten()
                .toSet()
    }

    /**
     * Adds a result.
     */
    fun addResult(value: Any) {
        requireNotNull(schema) { "schema is required" }

        val entityClass: Class<*> = value.javaClass
        val col = schema.getEntityDescriptors(entityClass)

        require(!col.isEmpty()) { "EntityDescriptor for ${entityClass.name} not found" }
        val pk: Property<*, *>? = col.findFirstByEntityClass<Property<*, *>>(entityClass)?.primaryKey
                ?: col.findFirstByGraphKey<Property<*, *>>(entityClass)?.primaryKey

        requireNotNull(pk) { "Primary key for ${entityClass.name} not found" }
        addResult(value.javaClass, pk.read(value), value)
    }

    /**
     * Adds many results.
     */
    fun <T : Any> addResults(type: Class<*>, idFunction: (T) -> Any, objects: Collection<T>) {
        for (o in objects) {
            addResult(type, idFunction(o), o)
        }
    }

    /**
     * Adds many results.
     */
    fun <T : Any> addResults(type: Class<*>, idFunction: (T) -> Any, objects: Array<T>) {
        for (o in objects) {
            addResult(type, idFunction(o), o)
        }
    }

    /**
     * Adds a result.
     */
    fun addResult(type: Class<*>, id: Any, value: Any) {
        val graphTypeKey = getGraphTypeKey(type)
        results.computeIfAbsent(graphTypeKey) { mutableMapOf() }[id] = value
    }

    /**
     * Returns a {@link Function} that delegates to
     * {@link #has(Class, Object)} for the given type.
     */
    fun hasFunction(type: Class<*>): (Any) -> Boolean {
        return { id -> has(type, id) }
    }

    /**
     * Returns a {@link Function} that delegates to
     * {@link #has(Class, Object)} for the given type.
     */
    fun hasPredicate(type: Class<*>): Predicate<Any> {
        return Predicate<Any> { id -> has(type, id) }
    }

    /**
     * Returns the object of the given type with
     * the given id.
     */
    fun has(type: Class<*>, id: Any): Boolean {
        val graphTypeKey = getGraphTypeKey(type)
        val map = results[graphTypeKey]
                ?: return false
        return map.containsKey(id)
    }

    /**
     * Returns a [Function] that delegates to
     * [.get] for the given type.
     */
    fun <T : Any> getFunction(type: Class<T>): (Any) -> T? {
        return { id -> get(type, id) }
    }

    /**
     * Returns the object of the given type with
     * the given id.
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(type: Class<T>, id: Any): T? = getMap<T>(type)[id]

    /**
     * Returns the object of the given type with
     * the given id.
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(type: Class<T>): List<T> = getList(type)

    /**
     * Returns the object of the given type with
     * the given id.
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> set(type: Class<T>, id: Any, value: T) = addResult(type, id, value)

    /**
     * Returns an immutable map of all items of a given type.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getMap(type: Class<*>): Map<Any, T> {
        val graphTypeKey = getGraphTypeKey(type)
        return results.computeIfAbsent(graphTypeKey) { mutableMapOf() }
                .toMap() as Map<Any, T>
    }

    /**
     * Returns a list of all items of a given type.
     */
    fun <T : Any> getList(type: Class<*>): List<T> {
        return getMap<T>(type)
                .map { it.value }
                .toList()
    }

    /**
     * Returns a list of all items of a given type.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> stream(type: Class<*>): Stream<T> {
        return getMap<T>(type).values.stream()
                .map { it as T }
    }

    /**
     * Returns a list of all items of a given type.
     */
    fun streamIds(type: Class<*>): Stream<Any> {
        return getMap<Any>(type).keys.stream()
    }

    /**
     * Returns a [Function] that delegates to
     * [.getList] for the given type.
     */
    fun <T : Any, I : Any> getListFunction(type: Class<*>): (Collection<I>) -> List<T> {
        return { ids: Collection<I> -> getList(type, ids) }
    }

    /**
     * Returns a list of all items of a given type with
     * the given ids - sorted in the same way as the ids.
     */
    fun <T : Any, I : Any> getList(type: Class<*>, ids: Collection<I>): List<T> {
        val map = getMap<T>(type)
        return ids.mapNotNull { map[it] }
                .toList()
    }

    /**
     * Returns the results map.  Be careful with it, it's
     * not immutable.
     */
    fun getResults(): MutableMap<Class<*>, MutableMap<Any, Any>> {
        return results
    }
}
