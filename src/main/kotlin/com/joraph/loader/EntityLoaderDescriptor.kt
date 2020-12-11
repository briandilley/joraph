package com.joraph.loader

class EntityLoaderDescriptor<ArgProvider, Arg, ID, Entity>(
    val argumentProviderClass: Class<ArgProvider>? = null,
    val argumentExtractor: ArgumentExtractor<ArgProvider, Arg>? = null,
    val entityClass: Class<Entity>,
    val loader: LoaderFunction<Arg, ID, Entity>) {

    fun requiresAdditionalArguments(): Boolean {
        return argumentProviderClass != null && argumentExtractor != null
    }
}
