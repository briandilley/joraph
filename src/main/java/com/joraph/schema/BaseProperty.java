package com.joraph.schema;

import static com.google.common.base.Preconditions.checkNotNull;

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
		this.descriptor = checkNotNull(descriptor, "descriptor must not be null");
	}

	@Override
	public String getName() {
		return descriptor.getPath();
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

	@Override
	public void write(Object obj, Object value) {
		try {
			assert(descriptor != null);
			descriptor.write(obj, value, false);
		} catch (Exception e) {
			throw new JoraphException(e);
		}
	}

}
