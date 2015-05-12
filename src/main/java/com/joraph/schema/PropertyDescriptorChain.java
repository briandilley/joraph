package com.joraph.schema;

import static java.util.Objects.requireNonNull;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A chain of {@link PropertyDescriptor}s so that one may use
 * dot notation to dig deep into an object graph.
 */
public class PropertyDescriptorChain {

	private PropertyDescriptor[] chain;
	private String path;

	/**
	 * Creates a {@link PropertyDescriptorChain} from the given class using
	 * the given path.
	 * @param path the path
	 * @param rootClass the root object
	 * @throws IntrospectionException
	 */
	public PropertyDescriptorChain(String path, Class<?> rootClass)
			throws IntrospectionException {
		this.path = path;

		String[] parts = path.split("\\.");
		this.chain = new PropertyDescriptor[parts.length];
		Class<?> clazz = rootClass;
		for (int i=0; i<parts.length; i++) {
			this.chain[i] = findPropertyDescriptor(parts[i], clazz);
			clazz = this.chain[i].getPropertyType();
		}

		if (chain.length<=0) {
			throw new IllegalStateException("chain.length<=0");
		}
	}

	/**
	 * A quick and dirty method for finding properties on a class.  This method
	 * isn't as strict as the default {@link PropertyDescriptor} constructor
	 * in that it allows for setters to have a return value.
	 * @param propertyName the property name
	 * @param clazz the class that the property belongs to
	 * @return the descriptor
	 * @throws IntrospectionException
	 */
	private static PropertyDescriptor findPropertyDescriptor(String propertyName, Class<?> clazz)
			throws IntrospectionException {
		for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
			if (pd.getName().equals(propertyName)) {
				return pd;
			}
		}
		throw new IntrospectionException("Property "+propertyName+" not found on "+clazz.getName());
	}

	/**
	 * Returns the path that this chain takes.
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Reads a value from the property descriptor chain.
	 * @param obj the root object
	 * @param failOnNullsInChain whether or not to fail on nulls in a chain
	 * @return the value, or null
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IllegalStateException if {@code failOnNullsInChain} and a non terminating
	 * link in the chain is null
	 */
	public Object read(Object obj, boolean failOnNullsInChain)
			throws IllegalAccessException,
			IllegalArgumentException,
			InvocationTargetException {
		Object ret = obj;
		for (int i=0; i<chain.length; i++) {
			Method readMethod = requireNonNull(
					chain[i].getReadMethod(), "Read method required"+chain[i]);
			ret = readMethod.invoke(obj);
			obj = ret;
			if (obj==null && i<chain.length-1) {
				if (failOnNullsInChain) {
					throw new IllegalStateException("Null link found in chain");
				} else {
					return null;
				}
			}
		}
		return ret;
	}

	/**
	 * Writes the value to the property descriptor chain.
	 * @param obj the root object
	 * @param value the value to write
	 * @param failOnNullsInChain whether or not to fail on nulls in a chain
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws IllegalStateException if {@code failOnNullsInChain} and a non terminating
	 * link in the chain is null
	 */
	public void write(Object obj, Object value, boolean failOnNullsInChain)
			throws IllegalAccessException,
			IllegalArgumentException,
			InvocationTargetException {
		Method writedMethod = null;
		for (int i=0; i<chain.length; i++) {
			if (i!=chain.length-1) {
				Method readMethod = requireNonNull(
						chain[i].getReadMethod(), "Read method required for "+chain[i]);
				obj = readMethod.invoke(obj);
				if (obj==null && i<chain.length-1) {
					if (failOnNullsInChain) {
						throw new IllegalStateException("Null link found in chain");
					} else {
						return;
					}
				}
			} else {
				writedMethod = requireNonNull(
						chain[i].getWriteMethod(), "Write method required for "+chain[i]);
				
			}
		}
		writedMethod.invoke(obj, value);
	}

}
