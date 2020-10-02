package com.joraph.schema;

import kotlin.jvm.functions.Function1;

/**
 * A key property.
 * @param <T> the property type
 */
public class Key<T, R>
		extends BaseProperty<T, R>
		implements Property<T, R> {

	/**
	 * Creates the key.
	 */
	public Key(Function1<T, R> accessor) {
		super.setPropertyChain(accessor);
	}

}
