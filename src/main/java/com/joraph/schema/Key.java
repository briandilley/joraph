package com.joraph.schema;

/**
 * A key property.
 * @param <T> the property type
 */
public class Key<T, R>
		extends BaseProperty<T, R>
		implements Property<T, R> {

	/**
	 * Creates the key.
	 * @param propertyName the property name
	 * @param entityClass the entity class
	 */
	public Key(PropertyDescriptorChain<T, R> chain) {
		super.setPropertyChain(chain);
	}

}
