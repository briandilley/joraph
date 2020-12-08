package com.joraph.schema

/**
 * A key property.
 * @param <T> the property type
 */
class Key<T, R>(accessor: Function1<T?, R?>) : BaseProperty<T, R>(accessor), Property<T, R>
