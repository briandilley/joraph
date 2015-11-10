package com.joraph.schema;

import java.util.function.Function;

/**
 * A composite key.
 * @param <T> the type representing the key
 */
public class CompositeKey<T>
		implements Property<T> {

	private BaseProperty<?>[] properties;
	private Function<Object[], T> converter;

	/**
	 * @param converter
	 * @param firstPropertyName
	 * @param secondPropertyName
	 * @param additionalPropertyNames
	 */
	public CompositeKey(
			Function<Object[], T> converter, PropertyDescriptorChain... chains) {
		this.converter 		= converter;
		this.properties		= new BaseProperty[chains.length];
		for (int i=0; i<properties.length; i++) {
			this.properties[i] = new BaseProperty<Object>(chains[i]);
		}
	}

	@Override
	public T read(Object obj) {
		Object[] ret = new Object[properties.length];
		for (int i=0; i<ret.length; i++) {
			ret[i] = properties[i].read(obj);
		}
		return converter.apply(ret);
	}

}
