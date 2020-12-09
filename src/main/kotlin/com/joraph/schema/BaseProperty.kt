package com.joraph.schema

import com.joraph.JoraphException

/**
 * Base class for `Property`.
 * @param <T> the property type</T>
 */
open class BaseProperty<T, R> constructor(val propertyAccessor: Function1<T, R?>) : Property<T, R> {

    @Suppress("UNCHECKED_CAST")
    override fun read(obj: Any): R? {
        return try {
            propertyAccessor.invoke(obj as T)
        } catch (e: Exception) {
            throw JoraphException(e)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseProperty<*, *>

        if (propertyAccessor != other.propertyAccessor) return false

        return true
    }

    override fun hashCode(): Int {
        return propertyAccessor.hashCode()
    }


}
