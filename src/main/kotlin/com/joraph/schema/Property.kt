package com.joraph.schema


/**
 * Base interface representing a property of an entity with an access method.
 */
@FunctionalInterface
interface Property<T, R> : (T) -> R? {
    override fun invoke(value: T): R? {
        return read(value as Any)
    }

    fun read(obj: Any): R?
}
