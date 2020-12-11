package com.joraph.loader

import com.joraph.JoraphException
import com.joraph.debug.JoraphDebug

/**
 * Manages all of the loaders responsible for loading entities. Each [com.joraph.JoraphContext] maintains
 * one [EntityLoaderContext]. This is the heart of the Joraph architecture. Joraph assumes that all entities
 * can be loaded by their primary key using a multi-get pattern. The [LoaderFunction] is the single interface
 * used by Joraph to load entities.
 */
open class EntityLoaderContext {

    private val loaders: MutableMap<Class<*>, EntityLoaderDescriptor<*, *, *, *>> = HashMap()

    /**
     * Starts and returns a builder for adding a [EntityLoaderDescriptorBuilder] to the context. Call
     * [EntityLoaderDescriptorBuilder.add] when finished building to add it.
     */
    open fun <Entity> addLoader(entityClass: Class<Entity>): EntityLoaderDescriptorBuilder<*, *, *, Entity> {
        return EntityLoaderDescriptorBuilder<Any, Any, Any, Entity>(this, entityClass)
    }

    /**
     * Adds the given [EntityLoaderDescriptor] to the context.
     */
    open fun <Entity> withLoader(entityClass: Class<Entity>, loader: EntityLoaderDescriptor<*, *, *, Entity>): EntityLoaderContext {
        loaders[entityClass] = loader
        return this
    }

    /**
     * Adds the given function as a [EntityLoaderDescriptor] to the context.
     */
    open fun <ID, Entity> withLoader(entityClass: Class<Entity>, loader: (Collection<ID>) -> Collection<Entity>): EntityLoaderContext {
        return withLoader(entityClass, EntityLoaderDescriptor<Any, Any, ID, Entity>(
            argumentProviderClass = null,
            argumentExtractor = null,
            entityClass = entityClass,
            loader = { _, ids -> loader(ids).toList() }))
    }

    /**
     * Adds the given [LoaderFunction] as a [EntityLoaderDescriptor] to the context.
     */
    open fun <ID, Entity> withLoader(entityClass: Class<Entity>, loader: LoaderFunction<Any, ID, Entity>): EntityLoaderContext {
        return withLoader(entityClass, EntityLoaderDescriptor<Any, Any, ID, Entity>(
            argumentProviderClass = null,
            argumentExtractor = null,
            entityClass = entityClass,
            loader = loader))
    }

    /**
     * Adds the given function as a [EntityLoaderDescriptor] to the context.
     */
    open fun <ArgProvider, Arg, ID, Entity> withLoader(
        argumentProviderClass: Class<ArgProvider>? = null,
        argumentExtractor: ArgumentExtractor<ArgProvider, Arg>? = null,
        entityClass: Class<Entity>,
        loader: (Arg?, Collection<ID>) -> Collection<Entity>): EntityLoaderContext {
        return withLoader(entityClass, EntityLoaderDescriptor(
            argumentProviderClass = argumentProviderClass,
            argumentExtractor = argumentExtractor,
            entityClass = entityClass,
            loader = LoaderFunction<Arg, ID, Entity> { arg, ids -> loader(arg, ids).toList() }))
    }

    /**
     * Adds the given [LoaderFunction] as a [EntityLoaderDescriptor] to the context.
     */
    open fun <ArgProvider, Arg, ID, Entity> withLoader(
        argumentProviderClass: Class<ArgProvider>? = null,
        argumentExtractor: ArgumentExtractor<ArgProvider, Arg>? = null,
        entityClass: Class<Entity>,
        loader: LoaderFunction<Arg, ID, Entity>): EntityLoaderContext {
        return withLoader(entityClass, EntityLoaderDescriptor(
            argumentProviderClass = argumentProviderClass,
            argumentExtractor = argumentExtractor,
            entityClass = entityClass,
            loader = loader))
    }

    /**
     * Returns the [EntityLoaderDescriptor] configured for the given entity
     * @throws UnconfiguredLoaderException if not found
     */
    @Suppress("UNCHECKED_CAST")
    open fun <ArgumentProvider, Arg, ID, Entity> getLoader(entityClass: Class<Entity>): EntityLoaderDescriptor<ArgumentProvider, Arg, ID, Entity> {
        return loaders[entityClass] as EntityLoaderDescriptor<ArgumentProvider, Arg, ID, Entity>?
            ?: throw UnconfiguredLoaderException(entityClass)
    }

    /**
     * Uses configured loaders to load entities of the given type with the given ids.
     */
    @Throws(UnconfiguredLoaderException::class, MissingLoaderArgumentException::class, JoraphException::class)
    open fun <ID, Entity> load(entityClass: Class<Entity>, ids: Iterable<ID>): List<Entity> {
        return load(entityClass, emptyList(), ids)
    }

    /**
     * Uses configured loaders to load entities of the given type with the given arguments and ids.
     */
    @Throws(UnconfiguredLoaderException::class, MissingLoaderArgumentException::class, JoraphException::class)
    open fun <ID, Entity> load(entityClass: Class<Entity>, arguments: List<Any?>, ids: Iterable<ID>): List<Entity> {

        // find the loader
        val loader: EntityLoaderDescriptor<Any, Any, ID, Entity> = getLoader(entityClass)
        if (loader.requiresAdditionalArguments() && arguments.isEmpty()) {
            throw MissingLoaderArgumentException(loader)
        }

        // get argument
        val argument: Any? = if (loader.requiresAdditionalArguments()) {
            val argumentProviderClass = loader.argumentProviderClass
                ?: throw IllegalStateException("loader.argumentProviderClass is null but requires additional arguments")
            val argumentExtractor = loader.argumentExtractor
                ?: throw IllegalStateException("loader.argumentExtractor is null but requires additional arguments")

            arguments
                .filterNotNull()
                .firstOrNull { argumentProviderClass.isInstance(it) }
                ?.let { argumentProviderClass.cast(it) }
                ?.let { argumentExtractor.invoke(it) }
        } else {
            null
        }

        // bail if requires additional argument
        if (argument == null && loader.requiresAdditionalArguments()) {
            throw MissingLoaderArgumentException(loader)
        }

        // get ids
        val idsToLoad = ids.toMutableList()
        return try {
            val start = System.currentTimeMillis()
            val ret = loader.loader.load(argument, idsToLoad)
            JoraphDebug.addLoaderDebug(entityClass, System.currentTimeMillis() - start, idsToLoad, ret)
            ret
        } catch (t: Throwable) {
            throw JoraphException("Error invoking loader: $loader with ids: ${idsToLoad.joinToString(separator = ",")}", t)
        }
    }
}
