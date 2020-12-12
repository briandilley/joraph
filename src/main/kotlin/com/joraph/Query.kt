package com.joraph

/**
 * A [Query] is used to define which objects in a graph to load. A [Query] starts
 * with one or more [rootObjects] from which their [com.joraph.schema.Schema] is consulted to determine
 * what additional objects in their graph should be loaded, the same logic is applied
 * to every object that is loaded by the [Query] until the entire graph has been
 * loaded.
 */
open class Query {

    val entityClasses: MutableSet<Class<*>> = mutableSetOf()
    val rootObjects: MutableSet<Any> = mutableSetOf()
    val arguments: MutableList<Any> = mutableListOf()

    var existingGraph: ObjectGraph? = null
        private set

    val hasExistingGraph get() = existingGraph != null

    constructor()
    constructor(vararg entityClasses: Class<*>) {
        withEntityClass(*entityClasses)
    }

    /**
     * Instructs the [Query] to load the dependencies of all of the given [entityClasses]
     * present in the [ObjectGraph].
     */
    fun withEntityClasses(entityClasses: Collection<Class<*>>): Query {
        this.entityClasses.addAll(entityClasses)
        return this
    }

    /**
     * Instructs the [Query] to load the dependencies of all of the given [entityClasses]
     * present in the [ObjectGraph].
     */
    fun withEntityClass(vararg entityClasses: Class<*>): Query {
        return withEntityClasses(entityClasses.toList())
    }

    /**
     * Instructs the [Query] to load the dependencies of all of the given [rootEntities]
     * and to add them and their dependencies to the [ObjectGraph].
     */
    fun withRootEntities(rootEntities: Collection<Any>): Query {
        return withRootObjects(rootEntities)
            .withEntityClasses(rootEntities.map { it::class.java })
    }

    /**
     * Instructs the [Query] to load the dependencies of all of the given [rootEntities]
     * and to add them and their dependencies to the [ObjectGraph].
     */
    fun withRootEntity(vararg rootEntities: Any): Query {
        return withRootObjects(rootEntities.toList())
            .withEntityClasses(rootEntities.map { it::class.java })
    }

    /**
     * Instructs the [Query] to add the given [rootObjects] to the [ObjectGraph].
     * This does not cause their dependencies to be loaded, for that use
     * [withRootEntities].
     */
    fun withRootObjects(rootObjects: Collection<Any>): Query {
        this.rootObjects.addAll(rootObjects)
        return this
    }

    /**
     * Instructs the [Query] to add the given [rootObjects] to the [ObjectGraph].
     * This does not cause their dependencies to be loaded, for that use
     * [withRootEntity].
     */
    fun withRootObject(vararg rootObjects: Any): Query {
        this.rootObjects.addAll(rootObjects.toList())
        return this
    }

    /**
     * Instructs the [Query] to put results into the given [ObjectGraph].
     */
    fun withExistingGraph(existingGraph: ObjectGraph?): Query {
        this.existingGraph = existingGraph
        return this
    }

    /**
     * Adds objects that can provide additional arguments to [com.joraph.loader.LoaderFunction]s
     * when executing the [Query].
     */
    fun withArgumentProviders(arguments: Collection<Any>): Query {
        this.arguments.addAll(arguments)
        return this
    }

    /**
     * Adds objects that can provide additional arguments to [com.joraph.loader.LoaderFunction]s
     * when executing the [Query].
     */
    fun withArgumentProvider(vararg arguments: Any): Query {
        return withArgumentProviders(arguments.toList())
    }

    @Deprecated(
        message = "Use withArgumentProviders",
        replaceWith = ReplaceWith("withArgumentProviders"))
    fun withArguments(arguments: Collection<Any>): Query {
        return withArgumentProviders(arguments)
    }

    @Deprecated(
        message = "Use withArgumentProvider",
        replaceWith = ReplaceWith("withArgumentProvider"))
    fun withArgument(vararg arguments: Any): Query {
        return withArgumentProviders(arguments.toList())
    }
}
