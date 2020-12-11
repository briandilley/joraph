package com.joraph.loader

import com.joraph.JoraphException
import com.joraph.debug.JoraphDebug

open class EntityLoaderContext {

    private val loaders: MutableMap<Class<*>, EntityLoaderDescriptor<*, *, *, *>> = HashMap()

    open fun <Entity> addLoader(entityClass: Class<Entity>): EntityLoaderDescriptorBuilder<*, *, *, Entity> {
        return EntityLoaderDescriptorBuilder<Any, Any, Any, Entity>(this, entityClass)
    }

    open fun <Entity> withLoader(entityClass: Class<Entity>, loader: EntityLoaderDescriptor<*, *, *, Entity>): EntityLoaderContext {
        loaders[entityClass] = loader
        return this
    }

    open fun <ID, Entity> withLoader(entityClass: Class<Entity>, loader: (Collection<ID>) -> Collection<Entity>): EntityLoaderContext {
        return withLoader(entityClass, EntityLoaderDescriptor<Any, Any, ID, Entity>(
            argumentProviderClass = null,
            argumentExtractor = null,
            entityClass = entityClass,
            loader = { _, ids -> loader(ids).toList() }))
    }

    open fun <ID, Entity> withLoader(entityClass: Class<Entity>, loader: LoaderFunction<Any, ID, Entity>): EntityLoaderContext {
        return withLoader(entityClass, EntityLoaderDescriptor<Any, Any, ID, Entity>(
            argumentProviderClass = null,
            argumentExtractor = null,
            entityClass = entityClass,
            loader = loader))
    }

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

    @Suppress("UNCHECKED_CAST")
    open fun <ArgumentProvider, Arg, ID, Entity> getLoader(entityClass: Class<Entity>): EntityLoaderDescriptor<ArgumentProvider, Arg, ID, Entity> {
        return loaders[entityClass] as EntityLoaderDescriptor<ArgumentProvider, Arg, ID, Entity>?
            ?: throw UnconfiguredLoaderException(entityClass)
    }

    @Throws(UnconfiguredLoaderException::class, MissingLoaderArgumentException::class, JoraphException::class)
    open fun <ID, Entity> load(entityClass: Class<Entity>, ids: Iterable<ID>): List<Entity> {
        return load(entityClass, emptyList(), ids)
    }

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
