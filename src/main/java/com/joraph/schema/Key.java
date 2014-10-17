package com.joraph.schema;

import java.beans.IntrospectionException;
import java.io.Serializable;

import com.joraph.util.ReflectionUtil;

/**
 * A key property.
 * @param <T> the property type
 */
public class Key<T extends Serializable>
		extends BaseProperty<T>
		implements Property<T> {

	private final Class<?> entityClass;

	/**
	 * Creates the key.
	 * @param propertyName the property name
	 * @param entityClass the entity class
	 */
	public Key(String propertyName, Class<?> entityClass) 
			throws IntrospectionException {
		this.entityClass = entityClass;
		super.setDescriptor(ReflectionUtil.getPropertyDescriptor(entityClass, propertyName));
	}

	/**
	 * @return the entityClass
	 */
	public Class<?> getEntityClass() {
		return entityClass;
	}

}
