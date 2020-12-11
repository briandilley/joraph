package com.joraph.loader

@FunctionalInterface
fun interface LoaderFunction<Arg, ID, Entity> {
    fun load(argument: Arg?, ids: List<ID>): List<Entity>
}
