package com.joraph

import com.joraph.loader.EntityLoaderContext
import com.joraph.schema.Schema
import java.util.concurrent.*

/**
 * The main point of contact for using Joraph. The [JoraphContext] maintains a [Schema] and a
 * [EntityLoaderContext] and creates [ExecutionContext]s for executing [Query]s through it's
 * [execute] method.
 */
open class JoraphContext @JvmOverloads constructor(
    open val schema: Schema,
    parallelExecutorCount: Int = 50) {

    val loaderContext: EntityLoaderContext = EntityLoaderContext()
    var executorService: ExecutorService
    var parallelExecutorDefaultTimeoutMillis = TimeUnit.SECONDS.toMillis(30)

    init {
        val executorService = ThreadPoolExecutor(
            parallelExecutorCount, parallelExecutorCount,
            0L, TimeUnit.MILLISECONDS,
            LinkedBlockingQueue()
        )
        executorService.rejectedExecutionHandler = RejectedExecutionHandler { r, _ -> r.run() }
        this.executorService = executorService
    }

    /**
     * Creates a new [ObjectGraph] configured with the context's [Schema]
     */
    open fun createEmptyGraph(): ObjectGraph {
        return ObjectGraph(schema)
    }

    /**
     * Executes the given [Query] and returns the resulting [ObjectGraph].
     */
    open fun execute(query: Query): ObjectGraph {
        return ExecutionContext(this, query).execute()
    }

    /**
     * A shortcut for executing a query for the given [entityClasses] and root [objects].
     */
    @JvmOverloads
    open fun execute(entityClasses: Collection<Class<*>>, objects: Collection<Any>, existingGraph: ObjectGraph? = null): ObjectGraph {
        return execute(Query()
            .withEntityClasses(entityClasses)
            .withRootObjects(objects)
            .withExistingGraph(existingGraph))
    }

    /**
     * A shortcut for executing a query for the given [entityClass] and root [objects].
     */
    @JvmOverloads
    open fun execute(entityClass: Class<*>, objects: Collection<Any>, existingGraph: ObjectGraph? = null): ObjectGraph {
        return execute(Query()
            .withEntityClass(entityClass)
            .withRootObjects(objects)
            .withExistingGraph(existingGraph))
    }

    /**
     * A shortcut for executing a query for the given [entityClass] and root [rootObject].
     */
    @JvmOverloads
    open fun execute(entityClass: Class<*>, rootObject: Any, existingGraph: ObjectGraph? = null): ObjectGraph {
        return execute(Query()
            .withEntityClass(entityClass)
            .withRootObject(rootObject)
            .withExistingGraph(existingGraph))
    }

    /**
     * A shortcut for executing a query for the given [rootObject].
     */
    @JvmOverloads
    open fun executeForRootObject(rootObject: Any, existingGraph: ObjectGraph? = null): ObjectGraph {
        return execute(Query()
            .withEntityClass(rootObject.javaClass)
            .withRootObject(rootObject)
            .withExistingGraph(existingGraph))
    }

    /**
     * Allows for loading entities of the type defined by [entityClass] by their keys [ids]
     * using the configured [EntityLoaderContext].
     */
    open fun <T> load(entityClass: Class<T>, ids: Collection<Any>): List<T> {
        return loaderContext.load(entityClass, ids)
    }

    /**
     * Loads entities of the type defined by [entityType] with the primary keys [ids]
     * into the [existingGraph].
     */
    open fun <T> supplement(
        existingGraph: ObjectGraph, entityType: Class<*>, ids: Collection<Any>): ObjectGraph {
        val objects = load(entityType, ids)
        return if (objects.isEmpty()) {
            existingGraph
        } else {
            execute(Query()
                .withEntityClass(entityType)
                .withRootObjects(objects)
                .withExistingGraph(existingGraph))
        }
    }

}

