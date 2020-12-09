package com.joraph

import com.joraph.debug.JoraphDebug
import com.joraph.loader.EntityLoaderContext
import com.joraph.schema.PrimaryKeyNullPointerException
import com.joraph.schema.Property
import com.joraph.schema.Schema
import com.joraph.schema.SchemaUtil.shouldLoad
import com.joraph.schema.UnknownEntityDescriptorException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * An execution context which brings together a [com.joraph.JoraphContext],
 * a single entity class, and the root objects.
 */
class ExecutionContext(
        val context: JoraphContext,
        val query: Query) {

    val schema: Schema = context.schema
    val objectGraph: ObjectGraph = if (query.hasExistingGraph()) query.existingGraph else ObjectGraph(context.schema)
    val loaderContext: EntityLoaderContext = context.loaderContext

    private val keysToLoad: KeysToLoad = KeysToLoad()

    /**
     *
     * Executes the plan, iterates the resulting operations, and returns the results.
     *
     * Subsequent calls to `execute` result in a cached [ObjectGraph].
     * @return the results derived from loading the associated objects supplied in the root
     * objects
     */
    fun execute(): ObjectGraph {
        addToResults(query.rootObjects)

        keysToLoad.clear()
        val descriptors = query.entityClasses
                .flatMap { clazz: Class<*>? -> schema.getEntityDescriptors(clazz) }
                .toMutableSet()


        // for each entity
        var keepLoading = true
        while (keepLoading) {

            // get all of the FKs
            for (desc in descriptors) {
                for ((_, value) in desc.foreignKeys) {
                    gatherValuesForForeignKeysTo(value.foreignEntity)
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
                    .flatMap { clazz: Class<*>? -> schema.getEntityDescriptors(clazz) }
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
