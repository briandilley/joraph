package com.joraph

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

    fun withEntityClasses(entityClasses: Collection<Class<*>>): Query {
        this.entityClasses.addAll(entityClasses)
        return this
    }

    fun withEntityClass(vararg entityClasses: Class<*>): Query {
        return withEntityClasses(entityClasses.toList())
    }

    fun withRootEntities(rootEntities: Collection<Any>): Query {
        return withRootObjects(rootEntities)
            .withEntityClasses(rootEntities.map { it::class.java })
    }

    fun withRootEntity(vararg rootEntities: Any): Query {
        return withRootObjects(rootEntities.toList())
    }

    fun withRootObjects(rootObjects: Collection<Any>): Query {
        this.rootObjects.addAll(rootObjects)
        return this
    }

    fun withRootObject(vararg rootObjects: Any): Query {
        this.rootObjects.addAll(rootObjects.toList())
        return this
    }

    fun withExistingGraph(existingGraph: ObjectGraph?): Query {
        this.existingGraph = existingGraph
        return this
    }

    fun withArgumentProviders(arguments: Collection<Any>): Query {
        this.arguments.addAll(arguments)
        return this
    }

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
