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
public class EntityDescriptor<T> {

	private final Class<T> entityClass;
	private Property<T, ?> primaryKey;
	private Map<PropertyDescriptorChain<T, ?>, ForeignKey<T, ?>> foreignKeys = new HashMap<>();

	/**
	 * Creates a new instance of EntityDescriptor.
	 * @param entityClass the entity class
	 */
	public EntityDescriptor(Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	/**
	 * @return the entityClass
	 */
	public Class<T> getEntityClass() {
		return entityClass;
	}

	/**
	 * @return the primaryKey
	 */
	public Property<T, ?> getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * @param primaryKey the primaryKey to set
	 * @throws IntrospectionException on error
	 * @return this
	 */
	public <RR> EntityDescriptor<T> setPrimaryKey(Function<T, RR> fun)
		throws IntrospectionException {
		this.primaryKey = new Key<>(new PropertyDescriptorChain<T, RR>(fun));
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
	@SafeVarargs
	@SuppressWarnings("unchecked") 
	public final EntityDescriptor<T> setPrimaryKey(
			Function<Object[], ?> converter,
			PropertyDescriptorChain<T, ?> first,
			PropertyDescriptorChain<T, ?>... remaining)
		throws IntrospectionException {
		PropertyDescriptorChain<T, ?>[] chains = CollectionUtil.asStream(first, remaining)
				.toArray(PropertyDescriptorChain[]::new);
		this.primaryKey = new CompositeKey<>(converter, chains);
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
	@SafeVarargs
	public final EntityDescriptor<T> setPrimaryKey(
			PropertyDescriptorChain<T, ?> first,
			PropertyDescriptorChain<T, ?>... remaining)
		throws IntrospectionException {
		return setPrimaryKey(BasicCompositeKey.CONVERTER, first, remaining);
	}
 
	/**
	 * @return the foreignKeys
	 */
	public Map<PropertyDescriptorChain<?, ?>, ForeignKey<?, ?>> getForeignKeys() {
		return Collections.unmodifiableMap(foreignKeys);
	}

	/**
	 * Returns a {@link ForeignKey} by name.
	 * @param propertyName the name
	 * @return the key
	 */
	public ForeignKey<T, ?> getForeignKey(Function<Object, ?> accessor) {
		return foreignKeys.get(new PropertyDescriptorChain<>(accessor));
	}

	/**
	 * Adds a foreign key.
	 * @param propertyName the property name
	 * @param foreignEntity the foreign entity
	 * @param eagar whether or not it's an eager relationship
	 * @throws IntrospectionException on error
	 * @return this
	 */
	public EntityDescriptor<T> addForeignKey(Class<?> foreignEntity, Function<T, ?> accessor)
		throws IntrospectionException {
		addForeignKey(foreignEntity, true, new PropertyDescriptorChain<>(accessor));
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
	public EntityDescriptor<T> addForeignKey(Class<?> foreignEntity, boolean eagar, Function<T, ?> accessor)
		throws IntrospectionException {
		return addForeignKey(foreignEntity, eagar, new PropertyDescriptorChain<>(accessor));
	}

	/**
	 * Adds a foreign key.
	 * @param propertyName the property name
	 * @param foreignEntity the foreign entity
	 * @param eagar whether or not it's an eager relationship
	 * @throws IntrospectionException on error
	 * @return this
	 */
	public EntityDescriptor<T> addForeignKey(Class<?> foreignEntity, PropertyDescriptorChain<T, ?> accessor)
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
	public EntityDescriptor<T> addForeignKey(Class<?> foreignEntity, boolean eagar, PropertyDescriptorChain<T, ?> accessor)
			throws IntrospectionException {
		this.foreignKeys.put(accessor, new ForeignKey<>(entityClass, foreignEntity, accessor));
		return this;
	}

}
