package com.joraph.schema;

import java.beans.IntrospectionException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EntityDescriptor {

	private Class<?> entityClass;
	private Key<?> primaryKey;
	private Map<String, ForeignKey<?>> foreignKeys = new HashMap<>();

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
	public Key<?> getPrimaryKey() {
		return primaryKey;
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
