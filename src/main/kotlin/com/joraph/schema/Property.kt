package com.joraph.schema


/**
 * Implementers have reflection-based runtime access to a
 * class's properties.
 * @param <T> the property type
 */
@FunctionalInterface
interface Property<T, R> : Function1<T, R?> {
    override fun invoke(value: T): R? {
        return read(value as Any)
    }

    fun read(obj: Any): R?
}
