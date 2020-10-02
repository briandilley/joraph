package com.joraph.schema;

import kotlin.jvm.functions.Function1;

/**
 * Implementers have reflection-based runtime access to a
 * class's properties.
 * @param <T> the property type
 */
@FunctionalInterface
public interface Property<T, R>
		extends Function1<T, R> {

	@Override
	default R invoke(T value) {
		return read(value);
	}

	R read(Object obj);

}
