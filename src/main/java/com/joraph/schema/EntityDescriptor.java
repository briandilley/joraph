package com.joraph.schema;

import static java.util.Objects.requireNonNull;

import java.beans.IntrospectionException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import com.joraph.CollectionUtil;

import kotlin.jvm.functions.Function1;

/**
 * Metadata about an entity class.
 */
public class EntityDescriptor<T> {

	private final Class<T> entityClass;
	private final Map<Function1<T, ?>, ForeignKey<T, ?>> foreignKeys = new HashMap<>();
	private Class<?> graphKey;
	private Property<T, ?> primaryKey;

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
	 */
	public <RR> EntityDescriptor<T> setPrimaryKey(Function1<T, RR> fun) {
		this.primaryKey = new Key<>(fun);
		return this;
	}

	/**
	 */
	public EntityDescriptor<T> setGraphKey(Class<?> graphKey) {
		this.graphKey = requireNonNull(graphKey, "graphKey cannot be null");
		return this;
	}

	/**
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked") 
	public final EntityDescriptor<T> setPrimaryKey(
			Function1<Object[], ?> converter,
			Function1<T, ?> first,
			Function1<T, ?>... remaining) {
		Function1<T, ?>[] chains = CollectionUtil.asStream(first, remaining)
				.toArray(Function1[]::new);
		this.primaryKey = new CompositeKey<>(converter, chains);
		return this;
	}

	/**
	 */
	@SafeVarargs
	public final EntityDescriptor<T> setPrimaryKey(Function1<T, ?> first, Function1<T, ?>... remaining) {
		return setPrimaryKey(BasicCompositeKey.CONVERTER, first, remaining);
	}
 
	/**
	 * @return the foreignKeys
	 */
	public Map<Function1<?, ?>, ForeignKey<?, ?>> getForeignKeys() {
		return Collections.unmodifiableMap(foreignKeys);
	}

	/**
	 * Creates a builder for adding a foreign key.
	 */
	public ForeignKeyBuilder<?, T> addForeignKey(Class<?> foreignEntity) {
		return new ForeignKeyBuilder<>(this, foreignEntity);
	}

	/**
	 * Adds a foreign key.
	 */
	public EntityDescriptor<T> addForeignKey(Class<?> foreignEntity, Function1<T, ?> accessor) {
		addForeignKey(foreignEntity, accessor, null, null);
		return this;
	}

	/**
	 * Adds a foreign key.
	 */
	public <A> EntityDescriptor<T> addForeignKey(Class<?> foreignEntity, Function1<T, ?> accessor, Class<A> argumentClass, Predicate<A> argumentPredicate)  {
		this.foreignKeys.put(accessor, new ForeignKey<>(entityClass, foreignEntity, argumentClass, argumentPredicate, accessor));
		return this;
	}

	/**
	 * Returns a {@link ForeignKey} by name.
	 */
	public ForeignKey<T, ?> getForeignKey(Function1<Object, ?> accessor) {
		return foreignKeys.get(accessor);
	}


	public class ForeignKeyBuilder<A, TT> {

		private final EntityDescriptor<TT> entity;
		private final Class<?> foreignEntity;
		private Function1<TT, ?> accessor;
		private Class<A> argumentClass;
		private Predicate<A> argumentPredicate;

		private ForeignKeyBuilder(EntityDescriptor<TT> entity, Class<?> foreignEntity) {
			this.entity = entity;
			this.foreignEntity = foreignEntity;
		}

		public ForeignKeyBuilder<A, TT> withAccessor(Function1<TT, ?> accessor) {
			this.accessor = accessor;
			return this;
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
