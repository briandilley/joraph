package com.joraph.schema;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * A chain of {@link PropertyDescriptor}s so that one may use
 * dot notation to dig deep into an object graph.
 */
public class PropertyDescriptorChain<T, R> {

	private Function<Object, ?>[] chain;

	public static <T, R> Builder<T, R> newChain(Function<T, R> accessor) {
		return Builder.newChain(accessor);
	}

	/**
	 * Creates a {@link PropertyDescriptorChain} from the given class using
	 * the given path.
	 * @param path the path
	 * @param rootClass the root object
	 */
	public PropertyDescriptorChain(Function<Object, ?>[] chain) {
		this.chain = chain;
		if (chain.length<=0) {
			throw new IllegalStateException("chain.length<=0");
		}
	}

	/**
	 * Creates a {@link PropertyDescriptorChain} from the given class using
	 * the given path.
	 * @param path the path
	 * @param rootClass the root object
	 */
	@SuppressWarnings("unchecked")
	public PropertyDescriptorChain(Function<T, R> accessor) {
		this(new Function[] { accessor });
	}

	/**
	 * Reads a value from the property descriptor chain.
	 * @param obj the root object
	 * @param failOnNullsInChain whether or not to fail on nulls in a chain
	 * @return the value, or null
	 * @throws IllegalStateException if {@code failOnNullsInChain} and a non terminating
	 * link in the chain is null
	 */
	@SuppressWarnings("unchecked")
	public R read(T obj, boolean failOnNullsInChain)
			throws IllegalAccessException,
			IllegalArgumentException,
			InvocationTargetException {
		Object ret = obj;
		for (int i=0; i<chain.length; i++) {
			ret = chain[i].apply(ret);
			if (ret==null && i<chain.length-1) {
				if (failOnNullsInChain) {
					throw new IllegalStateException("Null link found in chain");
				} else {
					return null;
				}
			}
		}
		return (R)ret;
	}

	@Override
	public String toString() {
		return Arrays.toString(chain);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(chain);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyDescriptorChain<?,?> other = (PropertyDescriptorChain<?,?>) obj;
		if (!Arrays.equals(chain, other.chain))
			return false;
		return true;
	}

	/**
	 * Builder for this object.
	 */
	public static class Builder<T, R> {

		private List<Function<?, ?>> accessors = new ArrayList<>();

		public static <T, R> Builder<T, R> newChain(Function<T, R> accessor) {
			Builder<T, R> ret = new Builder<>();
			ret.accessors.add(accessor);
			return ret;
		}

		@SuppressWarnings("unchecked")
		public <RR> Builder<T, RR> andThen(Function<R, RR> accessor) {
			this.accessors.add(accessor);
			return (Builder<T, RR>)this;
		}

		@SuppressWarnings("unchecked")
		public PropertyDescriptorChain<T, R> build() {
			return new PropertyDescriptorChain<>(accessors.stream()
					.toArray(Function[]::new));
		}

	}

}
