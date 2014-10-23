package com.joraph.schema;

import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import com.joraph.JoraphException;

/**
 * Base class for {@code Property}.
 * @param <T> the property type
 */
public class BaseProperty<T>
	implements Property<T> {

	private PropertyDescriptor descriptor;

	/**
	 */
	public BaseProperty() {
	}

	/**
	 * @param descriptor the descriptor to set
	 */
	public BaseProperty(PropertyDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	/**
	 * @param descriptor the descriptor to set
	 */
	protected void setDescriptor(PropertyDescriptor descriptor) {
		this.descriptor = checkNotNull(descriptor, "descriptor must not be null");
	}

	@Override
	public String getName() {
		return descriptor.getName();
	}

	@Override
	@SuppressWarnings("unchecked")
	public T read(Object obj) {
		try {
			assert(descriptor != null);
			final Method readMethod =
					checkNotNull(descriptor.getReadMethod(), "descriptor.getReadMethod must not be null");
			return (T)readMethod.invoke(obj);
		} catch (Exception e) {
			throw new JoraphException(e);
		}
	}

	@Override
	public void write(Object obj, Object value) {
		try {
			assert(descriptor != null);
			final Method writeMethod =
					checkNotNull(descriptor.getWriteMethod(), "descriptor.getWriteMethod must not be null");
			writeMethod.invoke(obj, value);
		} catch (Exception e) {
			throw new JoraphException(e);
		}
	}

}
