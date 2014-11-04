package com.joraph.schema;

import java.beans.IntrospectionException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Converter;

/**
 * Metadata about an entity class.
 */
public class EntityDescriptor {

	private final Class<?> entityClass;
	private Property<?> primaryKey;
	private Map<String, ForeignKey<?>> foreignKeys = new HashMap<>();

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
	 * 
	 * @param converter
	 * @param firstPropertyName
	 * @param secondPropertyName
	 * @param additionalPropertyNames
	 * @return
	 * @throws IntrospectionException
	 */
	public <T> EntityDescriptor setPrimaryKey(Converter<Object[], T> converter,
			String firstPropertyName, String secondPropertyName, String... additionalPropertyNames)
		throws IntrospectionException {
		this.primaryKey = new CompositeKey<T>(entityClass, converter,
				firstPropertyName, secondPropertyName, additionalPropertyNames);
		return this;
	}

	/**
	 * @param primaryKey the primaryKey to set
	 * @throws IntrospectionException on error
	 * @return this
	 */
	public EntityDescriptor setPrimaryKey(String propertyName)
		throws IntrospectionException {
		this.primaryKey = new Key<>(propertyName, entityClass);
		return this;
	}
 
	/**
	 * @return the foreignKeys
	 */
	public Map<String, ForeignKey<?>> getForeignKeys() {
		return Collections.unmodifiableMap(foreignKeys);
	}

	/**
	 * Returns a {@link ForeignKey} by name.
	 * @param propertyName the name
	 * @return the key
	 */
	public ForeignKey<?> getForeignKey(String propertyName) {
		return foreignKeys.get(propertyName);
	}

	/**
	 * Adds a foreign key.
	 * @param propertyName the property name
	 * @param foreignEntity the foreign entity
	 * @param eagar whether or not it's an eager relationship
	 * @throws IntrospectionException on error
	 * @return this
	 */
	public EntityDescriptor addForeignKey(String propertyName, Class<?> foreignEntity)
		throws IntrospectionException {
		addForeignKey(propertyName, foreignEntity, true);
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
	public EntityDescriptor addForeignKey(String propertyName, Class<?> foreignEntity, boolean eagar)
		throws IntrospectionException {
		this.foreignKeys.put(propertyName,
			new ForeignKey<>(propertyName, entityClass, foreignEntity));
		return this;
	}

}
