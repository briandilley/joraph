package com.joraph.schema;

import java.beans.IntrospectionException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.joraph.CollectionUtil;

/**
 * Metadata about an entity class.
 */
public class EntityDescriptor {

	private final Class<?> entityClass;
	private Property<?> primaryKey;
	private Map<PropertyDescriptorChain, ForeignKey<?>> foreignKeys = new HashMap<>();

	/**
	 * Creates a new instance of EntityDescriptor.
	 * @param entityClass the entity class
	 */
	public EntityDescriptor(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	/**
	 * @return the entityClass
	 */
	public Class<?> getEntityClass() {
		return entityClass;
	}

	/**
	 * @return the primaryKey
	 */
	public Property<?> getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * @param primaryKey the primaryKey to set
	 * @throws IntrospectionException on error
	 * @return this
	 */
	public <T> EntityDescriptor setPrimaryKey(Function<T, ?> fun)
		throws IntrospectionException {
		this.primaryKey = new Key<>(new PropertyDescriptorChain(fun));
		return this;
	}

	/**
	 * 
	 * @param converter
	 * @param firstPropertyName
	 * @param secondPropertyName
	 * @param additionalPropertyNames
	 * @return
	 * @throws IntrospectionException
	 */
	public <T> EntityDescriptor setPrimaryKey(
			Function<Object[], T> converter, PropertyDescriptorChain first, PropertyDescriptorChain... remaining)
		throws IntrospectionException {
		this.primaryKey = new CompositeKey<T>(converter, CollectionUtil.asStream(first, remaining)
				.toArray(PropertyDescriptorChain[]::new));
		return this;
	}

	/**
	 * 
	 * @param converter
	 * @param firstPropertyName
	 * @param secondPropertyName
	 * @param additionalPropertyNames
	 * @return
	 * @throws IntrospectionException
	 */
	public <T> EntityDescriptor setPrimaryKey(PropertyDescriptorChain first, PropertyDescriptorChain... remaining)
		throws IntrospectionException {
		return setPrimaryKey(BasicCompositeKey.CONVERTER, first, remaining);
	}
 
	/**
	 * @return the foreignKeys
	 */
	public Map<PropertyDescriptorChain, ForeignKey<?>> getForeignKeys() {
		return Collections.unmodifiableMap(foreignKeys);
	}

	/**
	 * Returns a {@link ForeignKey} by name.
	 * @param propertyName the name
	 * @return the key
	 */
	public ForeignKey<?> getForeignKey(Function<Object, ?> accessor) {
		return foreignKeys.get(new PropertyDescriptorChain(accessor));
	}

	/**
	 * Adds a foreign key.
	 * @param propertyName the property name
	 * @param foreignEntity the foreign entity
	 * @param eagar whether or not it's an eager relationship
	 * @throws IntrospectionException on error
	 * @return this
	 */
	public <T> EntityDescriptor addForeignKey(Class<?> foreignEntity, Function<T, ?> accessor)
		throws IntrospectionException {
		addForeignKey(foreignEntity, true, new PropertyDescriptorChain(accessor));
		return this;
	}

	/**
	 * Adds a foreign key.
	 * @param propertyName the property name
	 * @param foreignEntity the foreign entity
	 * @param eagar whether or not it's an eager relationship
	 * @throws IntrospectionException on error
	 * @return this
	 */
	public <T> EntityDescriptor addForeignKey(Class<?> foreignEntity, boolean eagar, Function<T, ?> accessor)
		throws IntrospectionException {
		return addForeignKey(foreignEntity, eagar, new PropertyDescriptorChain(accessor));
	}

	/**
	 * Adds a foreign key.
	 * @param propertyName the property name
	 * @param foreignEntity the foreign entity
	 * @param eagar whether or not it's an eager relationship
	 * @throws IntrospectionException on error
	 * @return this
	 */
	public <T> EntityDescriptor addForeignKey(Class<?> foreignEntity, PropertyDescriptorChain accessor)
		throws IntrospectionException {
		return addForeignKey(foreignEntity, true, accessor);
	}

	/**
	 * Adds a foreign key.
	 * @param propertyName the property name
	 * @param foreignEntity the foreign entity
	 * @param eagar whether or not it's an eager relationship
	 * @throws IntrospectionException on error
	 * @return this
	 */
	public <T> EntityDescriptor addForeignKey(Class<?> foreignEntity, boolean eagar, PropertyDescriptorChain accessor)
			throws IntrospectionException {
		this.foreignKeys.put(accessor, new ForeignKey<>(entityClass, foreignEntity, accessor));
		return this;
	}

}
