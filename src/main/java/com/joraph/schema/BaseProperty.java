package com.joraph.schema;

import static java.util.Objects.requireNonNull;

import com.joraph.JoraphException;

/**
 * Base class for {@code Property}.
 * @param <T> the property type
 */
public class BaseProperty<T, R>
	implements Property<T, R> {

	private PropertyDescriptorChain<T, R> descriptor;

	/**
	 */
	public BaseProperty() {
	}

	/**
	 * @param descriptor the descriptor to set
	 */
	public BaseProperty(PropertyDescriptorChain<T, R> descriptor) {
		setDescriptor(descriptor);
	}

	/**
	 * @param descriptor the descriptor to set
	 */
	protected void setDescriptor(PropertyDescriptorChain<T, R> descriptor) {
		this.descriptor = requireNonNull(descriptor, "descriptor must not be null");
	}

	/**
	 * @return descriptor the descriptor
	 */
	protected PropertyDescriptorChain<T, R> getDescriptor() {
		return this.descriptor;
	}

	@Override
	@SuppressWarnings("unchecked")
	public R read(Object obj) {
		try {
			return descriptor.read((T)obj, false);
		} catch (Exception e) {
			throw new JoraphException(e);
		}
	}

}
