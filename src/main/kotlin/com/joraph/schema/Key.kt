package com.joraph.schema

/**
 * A key property.
 * @param <T> the property type
 */
class Key<T, R>(accessor: Function1<T, R?>) : BaseProperty<T, R>(accessor), Property<T, R> {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        return true
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
