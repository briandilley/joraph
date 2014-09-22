package com.joraph.schema;

/**
 * Implementers have reflection-based runtime access to a
 * class's properties.
 * @param <T> the property type
 */
public interface Property<T> {

	String getName();

	T read(Object obj);

	void write(Object obj, T value);

}
