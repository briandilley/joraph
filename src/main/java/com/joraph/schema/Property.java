package com.joraph.schema;

/**
 * Implementers have reflection-based runtime access to a
 * class's properties.
 * @param <T> the property type
 */
@FunctionalInterface
public interface Property<T, R> {

	R read(Object obj);

}
