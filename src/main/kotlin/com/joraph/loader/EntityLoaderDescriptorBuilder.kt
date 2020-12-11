package com.joraph.loader

typealias ArgumentExtractor<ArgProvider, Arg> = ((ArgProvider) -> Arg?)

class EntityLoaderDescriptorBuilder<ArgProvider, Arg, ID, Entity>(
    private val context: EntityLoaderContext,
    private val entityClass: Class<Entity>) {

    private var argumentProviderClass: Class<ArgProvider>? = null
    private var argumentExtractor: ArgumentExtractor<ArgProvider, Arg>? = null
    private var loader: LoaderFunction<Arg, ID, Entity>? = null

    @Suppress("UNCHECKED_CAST")
    fun add(): EntityLoaderContext {
        return if (argumentProviderClass != null) {
            context.withLoader(argumentProviderClass!!, argumentExtractor!!, entityClass, loader!!)
        } else {
            context.withLoader(entityClass, (loader as LoaderFunction<Any, ID, Entity>?)!!)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <ArgProvider2, Arg2> expectingArgument(
        argumentProviderClass: Class<ArgProvider2>, argumentExtractor: Function1<ArgProvider2, Arg2>): EntityLoaderDescriptorBuilder<ArgProvider2, Arg2, ID, Entity> {
        val ret = this as EntityLoaderDescriptorBuilder<ArgProvider2, Arg2, ID, Entity>
        ret.argumentProviderClass = argumentProviderClass
        ret.argumentExtractor = argumentExtractor
        return ret
    }

    @Deprecated(
        message = "Deprecated in favor of expectingArgument",
        replaceWith = ReplaceWith("expectingArgument"))
    fun <ArgProvider2, Arg2> withArgument(
        argumentProviderClass: Class<ArgProvider2>, argumentExtractor: Function1<ArgProvider2, Arg2>): EntityLoaderDescriptorBuilder<ArgProvider2, Arg2, ID, Entity> {
        return expectingArgument(argumentProviderClass, argumentExtractor)
    }

    @Suppress("UNCHECKED_CAST")
    fun <ID2, Entity2> withLoaderFunction(func: LoaderFunction<Arg, ID2, Entity2>): EntityLoaderDescriptorBuilder<ArgProvider, Arg, ID2, Entity2> {
        val ret = this as EntityLoaderDescriptorBuilder<ArgProvider, Arg, ID2, Entity2>
        ret.loader = func
        return ret
    }

    @Suppress("UNCHECKED_CAST")
    fun <ID2, Entity2> withLoader(func: (arg: Arg?, ids: List<ID2>) -> Iterable<Entity2>): EntityLoaderDescriptorBuilder<ArgProvider, Arg, ID2, Entity2> {
        val ret = this as EntityLoaderDescriptorBuilder<ArgProvider, Arg, ID2, Entity2>
        ret.loader = LoaderFunction { arguments, ids -> func(arguments, ids).toMutableList() }
        return ret
    }

    @Suppress("UNCHECKED_CAST")
    fun <ID2, Entity2> withLoader(func: (ids: List<ID2>) -> Iterable<Entity2>): EntityLoaderDescriptorBuilder<ArgProvider, Arg, ID2, Entity2> {
        val ret = this as EntityLoaderDescriptorBuilder<ArgProvider, Arg, ID2, Entity2>
        ret.loader = LoaderFunction { _, ids -> func(ids).toMutableList() }
        return ret
    }

}


