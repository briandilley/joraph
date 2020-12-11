package com.joraph.loader

/**
 * The single interface used for loading entities. [LoaderFunction] assumes
 * a multi-get pattern by primary key.
 */
@FunctionalInterface
fun interface LoaderFunction<Arg, ID, Entity> {
    fun load(argument: Arg?, ids: List<ID>): List<Entity>
}
