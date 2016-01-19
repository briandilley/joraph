package com.joraph.schema;

import static java.util.Objects.requireNonNull;

import com.joraph.JoraphException;

/**
 * Base class for {@code Property}.
 * @param <T> the property type
 */
public class BaseProperty<T, R>
	implements Property<T, R> {

	private PropertyDescriptorChain<T, R> chain;

	/**
	 */
	public BaseProperty() {
	}

	/**
	 * @param chain the chain to set
	 */
	public BaseProperty(PropertyDescriptorChain<T, R> chain) {
		setPropertyChain(chain);
	}

	/**
	 * @param descriptor the descriptor to set
	 */
	protected void setPropertyChain(PropertyDescriptorChain<T, R> chain) {
		this.chain = requireNonNull(chain, "chain must not be null");
	}

	/**
	 * @return descriptor the descriptor
	 */
	protected PropertyDescriptorChain<T, R> getPropertyChain() {
		return this.chain;
	}

	@Override
	@SuppressWarnings("unchecked")
	public R read(Object obj) {
		try {
			return chain.read((T)obj, false);
		} catch (Exception e) {
			throw new JoraphException(e);
		}
	}

}
