package com.joraph.schema

/**
 * A foreign key property.
 * @param <T> the ID type
 */
open class ForeignKey<T, R>(
        val entityClass: Class<T>,
        val foreignEntity: Class<*>,
        accessor: Function1<T?, R?>) : BaseProperty<T, R>(accessor), Property<T, R> {

    override fun toString(): String {
        return (entityClass.name
                + "." + propertyAccessor + "->"
                + foreignEntity.name)
    }

}

