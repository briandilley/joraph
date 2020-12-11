package com.joraph.schema

import java.util.function.Predicate

/**
 * A [ForeignKey] that is conditional upon a specific argument being present and
 * a given predicate evaluating to true when the argument is passed to it.
 */
open class ConditionalForeignKey<T, R, A>(
        entityClass: Class<T>,
        foreignEntity: Class<*>,
        accessor: (T) -> R?,
        val argumentClass: Class<A>,
        val argumentPredicate: Predicate<A>) : ForeignKey<T, R>(entityClass, foreignEntity, accessor), Property<T, R> {

    /**
     * Indicates whether or not this foreign
     * key should be loaded.
     */
    @Suppress("UNCHECKED_CAST")
    fun shouldLoad(arguments: List<Any?>?): Boolean {
        if (arguments == null || arguments.isEmpty()) {
            return false
        }
        return arguments
                .filter { obj: Any? -> argumentClass.isInstance(obj) }
                .find { argumentPredicate.test(it as A) } != null
    }

    override fun toString(): String {
        return (entityClass.name
                + "." + propertyAccessor + "->"
                + foreignEntity.name
                + "(arg: " + argumentClass + ")")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ConditionalForeignKey<*, *, *>

        if (argumentClass != other.argumentClass) return false
        if (argumentPredicate != other.argumentPredicate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + argumentClass.hashCode()
        result = 31 * result + argumentPredicate.hashCode()
        return result
    }

}
