package com.joraph.schema

/**
 * A property that is a primary (unique) key. An entity can only
 * have a single primary key.
 */
class Key<T, R>(accessor: (T) -> R?) : BaseProperty<T, R>(accessor), Property<T, R> {

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
