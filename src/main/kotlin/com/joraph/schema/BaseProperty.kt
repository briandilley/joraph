package com.joraph.schema

import com.joraph.JoraphException

/**
 * Base class for `Property`.
 * @param <T> the property type</T>
 */
open class BaseProperty<T, R> constructor(val propertyAccessor: Function1<T?, R?>) : Property<T, R> {

    override fun read(obj: Any?): R? {
        return try {
            propertyAccessor.invoke(obj as T?)
        } catch (e: Exception) {
            throw JoraphException(e)
        }
    }
}
