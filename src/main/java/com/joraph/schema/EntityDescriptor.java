package com.joraph.schema;

import static java.util.Objects.requireNonNull;

import java.beans.IntrospectionException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.joraph.CollectionUtil;

/**
 * Metadata about an entity class.
 */
public class EntityDescriptor<T> {

	private final Class<T> entityClass;
	private Class<?> graphKey;
	private Property<T, ?> primaryKey;
	private Map<PropertyDescriptorChain<T, ?>, ForeignKey<T, ?>> foreignKeys = new HashMap<>();

	/**
	 * Creates a new instance of EntityDescriptor.
	 * @param entityClass the entity class
	 */
	public EntityDescriptor(Class<T> entityClass) {
		this.entityClass = entityClass;
		this.graphKey = entityClass;
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
	 * @return the graphKey
	 */
	public Class<?> getGraphKey() {
		return graphKey;
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
	 * @param graphKey the graphTypeKey to set
	 * @return this
	 */
	public EntityDescriptor<T> setGraphKey(Class<?> graphKey) {
		this.graphKey = requireNonNull(graphKey, "graphKey cannot be null");
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
	 * Creates a builder for adding a foreign key.
	 * @param foreignEntity the foreign entity
	 * @return this
	 */
	public ForeignKeyBuilder<?, T> addForeignKey(Class<?> foreignEntity)
			throws IntrospectionException {
		return new ForeignKeyBuilder<>(this, foreignEntity);
	}

	/**
	 * Adds a foreign key.
	 * @param propertyName the property name
	 * @param foreignEntity the foreign entity
	 * @throws IntrospectionException on error
	 * @return this
	 */
	public EntityDescriptor<T> addForeignKey(Class<?> foreignEntity, Function<T, ?> accessor)
			throws IntrospectionException {
		addForeignKey(foreignEntity, new PropertyDescriptorChain<>(accessor), null, null);
		return this;
	}

	/**
	 * Adds a foreign key.
	 * @param propertyName the property name
	 * @param foreignEntity the foreign entity
	 * @throws IntrospectionException on error
	 * @return this
	 */
	public <A> EntityDescriptor<T> addForeignKey(Class<?> foreignEntity, Function<T, ?> accessor, Class<A> argumentClass, Predicate<A> loadPredicate)
			throws IntrospectionException {
		return addForeignKey(foreignEntity, new PropertyDescriptorChain<>(accessor), argumentClass, loadPredicate);
	}

	/**
	 * Adds a foreign key.
	 * @param propertyName the property name
	 * @param foreignEntity the foreign entity
	 * @throws IntrospectionException on error
	 * @return this
	 */
	public EntityDescriptor<T> addForeignKey(Class<?> foreignEntity, PropertyDescriptorChain<T, ?> accessor)
			throws IntrospectionException {
		return addForeignKey(foreignEntity, accessor, null, null);
	}

	/**
	 * Adds a foreign key.
	 * @param propertyName the property name
	 * @param foreignEntity the foreign entity
	 * @param argumentPredicate an argument predicate for determining when
	 * to load the foreign key
	 * @throws IntrospectionException on error
	 * @return this
	 */
	public <A> EntityDescriptor<T> addForeignKey(Class<?> foreignEntity, PropertyDescriptorChain<T, ?> accessor, Class<A> argumentClass, Predicate<A> argumentPredicate)
			throws IntrospectionException {
		this.foreignKeys.put(accessor, new ForeignKey<>(entityClass, foreignEntity, argumentClass, argumentPredicate, accessor));
		return this;
	}

	/**
	 * Returns a {@link ForeignKey} by name.
	 * @param propertyName the name
	 * @return the key
	 */
	public ForeignKey<T, ?> getForeignKey(Function<Object, ?> accessor) {
		return foreignKeys.get(new PropertyDescriptorChain<>(accessor));
	}


	public class ForeignKeyBuilder<A, TT> {

		private EntityDescriptor<TT> entity;
		private Class<?> foreignEntity;
		private PropertyDescriptorChain<TT, ?> accessor;
		private Class<A> argumentClass;
		private Predicate<A> argumentPredicate;

		private ForeignKeyBuilder(EntityDescriptor<TT> entity, Class<?> foreignEntity) {
			this.entity = entity;
			this.foreignEntity = foreignEntity;
		}

		public ForeignKeyBuilder<A, TT> withAccessor(PropertyDescriptorChain<TT, ?> accessor) {
			this.accessor = accessor;
			return this;
		}

		public ForeignKeyBuilder<A, TT> withAccessor(Function<TT, ?> accessor) {
			return withAccessor(new PropertyDescriptorChain<>(accessor));
		}

		@SuppressWarnings("unchecked")
		public <A2> ForeignKeyBuilder<A2, TT> withPredicate(Class<A2> argumentClass, Predicate<A2> argumentPredicate) {
			this.argumentClass = (Class<A>)argumentClass;
			this.argumentPredicate = (Predicate<A>)argumentPredicate;
			return (ForeignKeyBuilder<A2, TT>)this;
		}

		public EntityDescriptor<TT> add()
				throws IntrospectionException {
			return entity.addForeignKey(foreignEntity, accessor, argumentClass, argumentPredicate);
		}

	}

}
