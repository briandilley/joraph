package com.joraph

import com.joraph.debug.JoraphDebug
import com.joraph.loader.EntityLoaderContext
import com.joraph.loader.LoaderFunction
import com.joraph.schema.PrimaryKeyNullPointerException
import com.joraph.schema.Property
import com.joraph.schema.Schema
import com.joraph.schema.SchemaUtil.shouldLoad
import com.joraph.schema.UnknownEntityDescriptorException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * An execution context is created for every [Query] that is executed. It uses the [Schema] configured
 * by the [JoraphContext] that it was created for to coordinate [LoaderFunction]s and to execute the [Query]
 * that it was created for. Technically an [ExecutionContext] could be used more than once, but it is
 * unnecessary to do so because it is not thread-safe.
 */
open class ExecutionContext @JvmOverloads constructor(
        val context: JoraphContext,
        val query: Query,
        var maxPasses: Int = 50) {

    val schema: Schema = context.schema
    val objectGraph: ObjectGraph = query.existingGraph ?: ObjectGraph(context.schema)
    val loaderContext: EntityLoaderContext = context.loaderContext

    private val keysToLoad: KeysToLoad = KeysToLoad()

    /**
     * Executes the [Query] and returns the resulting [ObjectGraph].
     */
    open fun execute(): ObjectGraph {
        addToResults(query.rootObjects)

        keysToLoad.clear()
        val descriptors = query.entityClasses
                .flatMap { schema.getEntityDescriptors(it) }
                .toMutableSet()

        // for each entity
        var keepLoading = true
        var pass = 0
        while (keepLoading) {

            pass++
            if (pass > maxPasses) {
                throw JoraphException("Maximum passes ($maxPasses) exceeded, do you have a circular dependency?")
            }

            // get all of the FKs
            for (desc in descriptors) {
                for (fk in desc.foreignKeys) {
                    gatherValuesForForeignKeysTo(fk.foreignEntity)
                }
            }

            // figure out which entities to load based on the FKs
            val entitiesToLoad = keysToLoad.entitiesToLoad

            // load the new entities
            if (entitiesToLoad.size == 1) {
                loadEntities(entitiesToLoad.first())
            } else if (entitiesToLoad.size > 1) {
                loadEntitiesInParallel(entitiesToLoad)
            }

            // add the new descriptors to the list
            val newDescriptors = descriptors.addAll(entitiesToLoad
                    .flatMap { schema.getEntityDescriptors(it) }
                    .toSet())
            keepLoading = entitiesToLoad.isNotEmpty() || newDescriptors
        }


        JoraphDebug.addObjectGraph(objectGraph)
        return objectGraph
    }

    private fun addToResults(objects: Iterable<*>?) {
        if (objects == null) {
            return
        }

        for (obj in objects) {
            if (obj == null) {
                continue
            }

            val entityDescriptor = schema.getEntityDescriptors(obj.javaClass)
                    .firstOrNull { it.entityClass == obj.javaClass }
                    ?: throw UnknownEntityDescriptorException(obj.javaClass)

            val pk: Property<*, *> = entityDescriptor.primaryKey

            val pkValue = pk.read(obj) ?: throw PrimaryKeyNullPointerException(obj.javaClass)

            objectGraph.addResult(entityDescriptor.graphKey, pkValue, obj)
        }
    }

    private fun gatherValuesForForeignKeysTo(entityClass: Class<*>) {
        context.schema.getEntityDescriptors(entityClass)
                .flatMap { ed -> context.schema.describeForeignKeysTo(entityClass)
                        .filter { fk -> shouldLoad(fk, query.arguments) }
                        .flatMap { fk ->  objectGraph.getList(fk.entityClass)
                                .filter { o -> o!!.javaClass == fk.entityClass }
                                .mapNotNull { obj -> fk.read(obj!!) }
                                .flatMap { id -> CollectionUtil.convertToSet(id) } // because it could be a one to many
                                .filterNotNull()
                                .filter { id -> !objectGraph.has(ed.entityClass, id) }}}
                .forEach(keysToLoad.getAddKeyToLoadFunction(entityClass))
    }

    private fun loadEntities(entityClass: Class<*>) {
        val ids = keysToLoad.getKeysToLoad(entityClass)
        if (ids.isEmpty()) {
            return
        }
        val objects = loaderContext.load(entityClass, query.arguments, ids)

        addToResults(objects)
        keysToLoad.addKeysLoaded(entityClass, ids)
    }

    private fun loadEntitiesInParallel(entityClasses: Collection<Class<*>>) {
        val futures = mutableListOf<Future<*>>()
        for (entityClass in entityClasses) {
            val info = JoraphDebug.getDebugInfo()
            futures.add(context.executorService.submit {
                JoraphDebug.setThreadDebugInfo(info)
                loadEntities(entityClass)
                JoraphDebug.clearThreadDebugInfo()
            })
            JoraphDebug.setThreadDebugInfo(info)
        }
        for (future in futures) {
            try {
                future.get(context.parallelExecutorDefaultTimeoutMillis, TimeUnit.MILLISECONDS)
            } catch (e: Throwable) {
                throw JoraphException(e)
            }
        }
    }

}

/**
 * Simple class for managing the keys that need to be loaded and those
 * that have already been loaded.
 */
internal class KeysToLoad {

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
