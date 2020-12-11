package com.joraph

import com.joraph.schema.Property
import com.joraph.schema.Schema
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Stream

/**
 * The [ObjectGraph] contains all of the entities from one more more graph as defined by
 * a [Schema]. The result of a [Query] is an [ObjectGraph]. This can be thought of as a
 * [Map] of [Map]s where the first [Map] is keyed by the entity [Class] and it's value
 * is a [Map] of that entity's id to the entity itself. For instance, to get a Person
 * with the id 34:
 * ```
 * val person = objectGraph.get(Person.class).get(34)
 * ```
 * or:
 * ```
 * val person = objectGraph[Person::class.java][34]
 * ```
 * This class is thread-safe and can be used on it's own, and with our without a [Schema],
 * although some methods require a schema to be present.
 */
open class ObjectGraph @JvmOverloads constructor(private val schema: Schema? = null) :
        Cloneable,
        Iterable<Pair<Class<*>, MutableMap<Any, Any>>> {

    val results: MutableMap<Class<*>, MutableMap<Any, Any>> = ConcurrentHashMap()

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
        return schema?.getGraphTypeKey(entityClass) ?: entityClass
    }

    /**
     * Copy this [ObjectGraph]'s results to the given [ObjectGraph].
     */
    fun copyGraphTo(destinationObjectGraph: ObjectGraph) {
        for ((key, value) in this) {
            value.putAll(destinationObjectGraph.results.computeIfAbsent(key) { ConcurrentHashMap() })
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
                .map { getList(it.entityClass)
                        .map { o -> it.primaryKey.read(o!!) as T } }
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
        val id = requireNotNull(pk.read(value)) { "Primary key cannot be null" }

        addResult(value.javaClass, id, value)
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
        results.computeIfAbsent(graphTypeKey) { ConcurrentHashMap() }[id] = value
    }

    /**
     * Returns the object of the given type with
     * the given id.
     */
    fun has(type: Class<*>, id: Any?): Boolean {
        if (id == null) {
            return false
        }
        val graphTypeKey = getGraphTypeKey(type)
        val map = results[graphTypeKey]
                ?: return false
        return map.containsKey(id)
    }

    /**
     * Returns the object of the given type with
     * the given id.
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(type: Class<T>, id: Any?): T? = id?.let { getMap<T>(type)[it] }

    /**
     * Returns the object of the given type with
     * the given id, throws an exception if it's not found.
     */
    fun <T : Any> getRequired(type: Class<T>, id: Any?): T {
        return get(type, id)
                ?: throw EntityNotFoundException("Entity of type ${type.name} with id $id not found")
    }

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
        return results.computeIfAbsent(graphTypeKey) { ConcurrentHashMap() }
                .toMap() as Map<Any, T>
    }

    /**
     * Returns a list of all items of a given type.
     */
    fun <T : Any> getList(type: Class<T>): List<T> {
        return getMap<T>(type)
                .map { it.value }
                .toList()
    }

    /**
     * Returns a list of all items of a given type.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> stream(type: Class<T>): Stream<T> {
        return getMap<T>(type).values.stream()
                .map { it as T }
    }

    /**
     * Returns a list of all items of a given type.
     */
    fun <T : Any> streamIds(type: Class<T>): Stream<Any> {
        return getMap<Any>(type).keys.stream()
    }

    /**
     * Returns a list of all items of a given type with
     * the given ids - sorted in the same way as the ids.
     */
    fun <T : Any, I : Any> getList(type: Class<T>, ids: Collection<I?>): List<T> {
        val map = getMap<T>(type)
        return ids
                .filterNotNull()
                .mapNotNull { map[it] }
                .toList()
    }

}
