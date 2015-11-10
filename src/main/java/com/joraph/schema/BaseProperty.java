package com.joraph.schema;

import static java.util.Objects.requireNonNull;

import com.joraph.JoraphException;

/**
 * Base class for {@code Property}.
 * @param <T> the property type
 */
public class BaseProperty<T>
	implements Property<T> {

	private PropertyDescriptorChain descriptor;

	/**
	 */
	public BaseProperty() {
	}

	/**
	 * @param descriptor the descriptor to set
	 */
	public BaseProperty(PropertyDescriptorChain descriptor) {
		this.descriptor = descriptor;
	}

	/**
	 * @param descriptor the descriptor to set
	 */
	protected void setDescriptor(PropertyDescriptorChain descriptor) {
		this.descriptor = requireNonNull(descriptor, "descriptor must not be null");
	}

	/**
	 * @return descriptor the descriptor
	 */
	protected PropertyDescriptorChain getDescriptor() {
		return this.descriptor;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T read(Object obj) {
		try {
			assert(descriptor != null);
			return (T)descriptor.read(obj, false);
		} catch (Exception e) {
			throw new JoraphException(e);
		}
	}

}
