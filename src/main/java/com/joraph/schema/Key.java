package com.joraph.schema;

import java.io.Serializable;

/**
 * A key property.
 * @param <T> the property type
 */
public class Key<T extends Serializable>
		extends BaseProperty<T>
		implements Property<T> {

	/**
	 * Creates the key.
	 * @param propertyName the property name
	 * @param entityClass the entity class
	 */
	public Key(PropertyDescriptorChain chain) {
		super.setDescriptor(chain);
	}

}
