package com.joraph.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A composite key.
 * @param <T> the type representing the key
 */
public class CompositeKey<T, R>
		implements Property<T, R> {

	private List<BaseProperty<T, ?>> properties;
	private Function<Object[], R> converter;

	/**
	 * @param converter
	 * @param firstPropertyName
	 * @param secondPropertyName
	 * @param additionalPropertyNames
	 */
	@SafeVarargs
	public CompositeKey(
			Function<Object[], R> converter, PropertyDescriptorChain<T, ?>... chains) {
		this.converter 		= converter;
		this.properties		= new ArrayList<>(chains.length);
		for (int i=0; i<chains.length; i++) {
			this.properties.add(new BaseProperty<>(chains[i]));
		}
	}

	@Override
	public R read(Object obj) {
		Object[] ret = new Object[properties.size()];
		for (int i=0; i<ret.length; i++) {
			ret[i] = properties.get(i).read(obj);
		}
		return converter.apply(ret);
	}

}
