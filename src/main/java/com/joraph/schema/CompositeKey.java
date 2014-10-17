package com.joraph.schema;

import java.beans.IntrospectionException;

import com.google.common.base.Converter;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.joraph.util.ReflectionUtil;

/**
 * A composite key.
 * @param <T> the type representing the key
 */
public class CompositeKey<T>
		implements Property<T> {

	private String[] propertyNames;
	private BaseProperty<?>[] properties;
	private Converter<Object[], T> converter;
	private Converter<T, Object[]> reverseConverter;

	/**
	 * @param entityClass
	 * @param converter
	 * @param firstPropertyName
	 * @param secondPropertyName
	 * @param additionalPropertyNames
	 * @throws IntrospectionException
	 */
	public CompositeKey(Class<?> entityClass, Converter<Object[], T> converter,
			String firstPropertyName, String secondPropertyName, String... additionalPropertyNames)
			throws IntrospectionException {
		this.propertyNames 		= Lists.asList(firstPropertyName, secondPropertyName, additionalPropertyNames).toArray(new String[0]);
		this.converter 			= converter;
		this.reverseConverter	= converter.reverse();
		this.properties			= new BaseProperty[propertyNames.length];
		for (int i=0; i<propertyNames.length; i++) {
			this.properties[i] = new BaseProperty<Object>(
					ReflectionUtil.getPropertyDescriptor(entityClass, propertyNames[i]));
		}
	}

	@Override
	public String getName() {
		return Joiner.on(", ").join(propertyNames);
	}

	@Override
	public T read(Object obj) {
		Object[] ret = new Object[propertyNames.length];
		for (int i=0; i<ret.length; i++) {
			ret[i] = properties[i].read(obj);
		}
		return converter.convert(ret);
	}

	@Override
	public void write(Object obj, T value) {
		Object[] values = reverseConverter.convert(value);
		for (int i=0; i<values.length; i++) {
			properties[i].write(obj, values[i]);
		}
	}

}
