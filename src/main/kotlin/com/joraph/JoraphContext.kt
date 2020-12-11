package com.joraph

import com.joraph.loader.EntityLoaderContext
import com.joraph.schema.Schema
import java.util.concurrent.*

/**
 * The main class for using Joraph.
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

    open fun createEmptyGraph(): ObjectGraph {
        return ObjectGraph(schema)
    }

    open fun execute(query: Query): ObjectGraph {
        return ExecutionContext(this, query).execute()
    }

    @JvmOverloads
    open fun execute(entityClasses: Collection<Class<*>>, objects: Collection<Any>, existingGraph: ObjectGraph? = null): ObjectGraph {
        return execute(Query()
            .withEntityClasses(entityClasses)
            .withRootObjects(objects)
            .withExistingGraph(existingGraph))
    }

    @JvmOverloads
    open fun execute(entityClass: Class<*>, objects: Collection<Any>, existingGraph: ObjectGraph? = null): ObjectGraph {
        return execute(Query()
            .withEntityClass(entityClass)
            .withRootObjects(objects)
            .withExistingGraph(existingGraph))
    }

    @JvmOverloads
    open fun execute(entityClass: Class<*>, rootObject: Any, existingGraph: ObjectGraph? = null): ObjectGraph {
        return execute(Query()
            .withEntityClass(entityClass)
            .withRootObject(rootObject)
            .withExistingGraph(existingGraph))
    }

    @JvmOverloads
    open fun executeForRootObject(rootObject: Any, existingGraph: ObjectGraph? = null): ObjectGraph {
        return execute(Query()
            .withEntityClass(rootObject.javaClass)
            .withRootObject(rootObject)
            .withExistingGraph(existingGraph))
    }

    open fun <T> load(entityClass: Class<T>, ids: Collection<Any>): List<T> {
        return loaderContext.load(entityClass, ids)
    }

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

