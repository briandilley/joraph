package com.joraph.schema;

import static java.util.Objects.requireNonNull;

import com.joraph.JoraphException;

import kotlin.jvm.functions.Function1;

/**
 * Base class for {@code Property}.
 * @param <T> the property type
 */
public class BaseProperty<T, R>
	implements Property<T, R> {

	private Function1<T, R> accessor;

	/**
	 */
	public BaseProperty() {
	}

	/**
	 * @param accessor the accessor to set
	 */
	public BaseProperty(Function1<T, R> accessor) {
		setPropertyChain(accessor);
	}

	/**
	 */
	protected void setPropertyChain(Function1<T, R> accessor) {
		this.accessor = requireNonNull(accessor, "accessor must not be null");
	}

	/**
	 * @return descriptor the descriptor
	 */
	protected Function1<T, R> getPropertyAccessor() {
		return this.accessor;
	}

	@Override
	@SuppressWarnings("unchecked")
	public R read(Object obj) {
		try {
			return accessor.invoke((T)obj);
		} catch (Exception e) {
			throw new JoraphException(e);
		}
	}

}
