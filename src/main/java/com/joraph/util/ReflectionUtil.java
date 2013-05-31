package com.joraph.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import com.joraph.JoraphException;

/**
 * Reflection utilities.
 */
public final class ReflectionUtil {

	/**
	 * Gets a {@link PropertyDescriptor} for the given {@code propertyName}
	 * on the given {@code clazz}.
	 * @param clazz the class
	 * @param propertyName the property name
	 * @return the descriptor, or null if not found
	 * @throws IntrospectionException on error
	 */
	public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName)
		throws IntrospectionException {
		for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
			if (pd.getName().equals(propertyName)) {
				return pd;
			}
		}
		throw new JoraphException("Property "+propertyName+" not found on "+clazz.getName());
	}
}
