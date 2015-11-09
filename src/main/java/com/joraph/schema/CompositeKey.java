package com.joraph.schema;

import java.beans.IntrospectionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A composite key.
 * @param <T> the type representing the key
 */
public class CompositeKey<T>
		implements Property<T> {

	private String[] propertyNames;
	private BaseProperty<?>[] properties;
	private Function<Object[], T> converter;
	private Function<T, Object[]> reverseConverter;

	/**
	 * @param entityClass
	 * @param converter
	 * @param firstPropertyName
	 * @param secondPropertyName
	 * @param additionalPropertyNames
	 * @throws IntrospectionException
	 */
	public CompositeKey(Class<?> entityClass,
			Function<Object[], T> converter, Function<T, Object[]> reverseConverter,
			String firstPropertyName, String secondPropertyName, String... additionalPropertyNames)
			throws IntrospectionException {
		this.propertyNames= Stream.concat(
				Stream.of(additionalPropertyNames),
				Stream.of(firstPropertyName, secondPropertyName)
			).toArray(String[]::new);
		this.converter 			= converter;
		this.reverseConverter	= reverseConverter;
		this.properties			= new BaseProperty[propertyNames.length];
		for (int i=0; i<propertyNames.length; i++) {
			this.properties[i] = new BaseProperty<Object>(
					new PropertyDescriptorChain(propertyNames[i], entityClass));
		}
	}

	@Override
	public String getName() {
		return Stream.of(propertyNames)
				.collect(Collectors.joining(","));
	}

	@Override
	public T read(Object obj) {
		Object[] ret = new Object[propertyNames.length];
		for (int i=0; i<ret.length; i++) {
			ret[i] = properties[i].read(obj);
		}
		return converter.apply(ret);
	}

	@Override
	public void write(Object obj, T value) {
		Object[] values = reverseConverter.apply(value);
		for (int i=0; i<values.length; i++) {
			properties[i].write(obj, values[i]);
		}
	}

}
