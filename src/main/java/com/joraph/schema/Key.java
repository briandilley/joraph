package com.joraph.schema;

import java.beans.IntrospectionException;
import java.io.Serializable;

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
		super.setDescriptor(new PropertyDescriptorChain(propertyName, entityClass));
	}

	/**
	 * @return the entityClass
	 */
	public Class<?> getEntityClass() {
		return entityClass;
	}

}
