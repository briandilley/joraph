package com.joraph.schema;

import java.util.ArrayList;
import java.util.List;

import kotlin.jvm.functions.Function1;

/**
 * A composite key.
 * @param <T> the type representing the key
 */
public class CompositeKey<T, R>
		implements Property<T, R> {

	private final List<BaseProperty<T, ?>> properties;
	private final Function1<Object[], R> converter;

	/**
	 */
	@SafeVarargs
	public CompositeKey(Function1<Object[], R> converter, Function1<T, ?>... accessors) {
		this.converter 		= converter;
		this.properties		= new ArrayList<>(accessors.length);
		for (Function1<T, ?> accessor : accessors) {
			this.properties.add(new BaseProperty<>(accessor));
		}
	}

	@Override
	public R read(Object obj) {
		Object[] ret = new Object[properties.size()];
		for (int i=0; i<ret.length; i++) {
			ret[i] = properties.get(i).read(obj);
		}
		return converter.invoke(ret);
	}

}
