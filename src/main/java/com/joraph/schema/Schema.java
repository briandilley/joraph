package com.joraph.schema;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import kotlin.jvm.functions.Function1;

/**
 * A schema.
 */
public class Schema {

	public static final Comparator<Class<?>> CLASS_COMPARATOR
		= new Comparator<Class<?>>() {
			@Override
			public int compare(Class<?> o1, Class<?> o2) {
				return o1.getName().compareTo(o2.getName());
			}
		};

	private boolean validated = false;
	private final Map<Class<?>, EntityDescriptor<?>> entityDescriptors = new HashMap<>();

	private void dirty() {
		this.validated = false;
	}

	/**
	 * @return whether the schema has been validated
	 */
	public boolean isValidated() {
		return validated;
	}

	/**
	 * Asserts that the schema has been validated.
	 */
	public void assertValidated() {
		// TODO remove this method and just validate at the appropriate call sites
		if (!isValidated()) {
			throw new IllegalStateException("Schema not validated");
		}
	}

	/**
	 * Returns the graph type key for the given entity class.
	 * @param entityClass the entity class
	 * @return the key
	 */
	public Class<?> getGraphTypeKey(Class<?> entityClass) {
		return Optional.ofNullable(entityDescriptors.get(entityClass))
				.map(EntityDescriptor::getGraphKey)
				.map(Class.class::cast)
				.orElse(entityClass);
	}

	/**
	 * @param entityClass the entity class
	 * @return the descriptor for the class, or null if no descriptor exists for it
	 */
	public Set<EntityDescriptor<?>> getEntityDescriptors(Class<?> entityClass) {
		return this.entityDescriptors.values().stream()
				.filter((ed) -> ed.getEntityClass().equals(entityClass)
					|| ed.getGraphKey().equals(entityClass))
				.collect(Collectors.toSet());
	}

	/**
	 * Adds an entity descriptor to the schema.
	 * @param entityDescriptor the entity descriptor to add
	 */
	public void addEntityDescriptor(EntityDescriptor<?> entityDescriptor) {
		this.dirty();
		this.entityDescriptors.put(entityDescriptor.getEntityClass(), entityDescriptor);
	}

	/**
	 * Wraps a class in an {@link com.joraph.schema.EntityDescriptor} and then
	 * adds it to the schema.
	 * @param entityClass the entity class
	 * @return the {@link com.joraph.schema.EntityDescriptor} created as a wrapper and
	 * added to the schema
	 */
	public <T> EntityDescriptor<T> addEntityDescriptor(Class<T> entityClass) {
		this.dirty();
		EntityDescriptor<T> ret = new EntityDescriptor<>(entityClass);
		addEntityDescriptor(ret);
		return ret;
	}

	/**
	 * Describes the foreign keys from one class to another, if any exist.
	 * @param fromEntityClass the from entity class
	 * @param toEntityClass the to entity class
	 * @return all of the foreign keys which exist from one entity class to
	 *         another or an empty collection if no such relationships exist
	 */
	public Collection<ForeignKey<?, ?>> describeForeignKeys(Class<?> fromEntityClass, Class<?> toEntityClass) {
		return describeForeignKeysFrom(fromEntityClass).stream()
				.filter((fk) -> fk.getForeignEntity().equals(toEntityClass))
				.collect(Collectors.toList());
	}

	/**
	 * Describes the foreign keys configured for a specified class.
	 * @param entityClass the entity class
	 * @return the foreign keys configured for that class
	 */
	public Collection<ForeignKey<?, ?>> describeForeignKeysFrom(Class<?> entityClass) {
		return getEntityDescriptors(entityClass).stream()
				.flatMap((entityDescriptor) ->
						entityDescriptor.getForeignKeys().values().stream())
				.collect(Collectors.toList());
	}

	/**
	 * Describes the foreign keys coming into a given entity class.
	 * @param toEntityClass the entity class
	 * @return foreign keys that point to an entity class
	 */
	public Collection<ForeignKey<?, ?>> describeForeignKeysTo(Class<?> toEntityClass) {
		return entityDescriptors.values().stream()
				.map(EntityDescriptor::getForeignKeys)
				.map(Map::values)
				.flatMap(Collection::stream)
				.filter((fk) -> fk.getForeignEntity().equals(toEntityClass))
				.collect(Collectors.toList());
	}

	/**
	 * Describes all of the foreign keys that have been configured.
	 * @return all of the configured foreign keys
	 */
	public Collection<ForeignKey<?, ?>> describeForeignKeys() {
		return entityDescriptors.values().stream()
				.map(EntityDescriptor::getForeignKeys)
				.map(Map::values)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	/**
	 * Describes the given entity class.
	 */
	public Set<Class<?>> describeEntities() {
		this.assertValidated();
		return Collections.unmodifiableSet(entityDescriptors.keySet());
	}

	/**
	 * Validates the {@link Schema}.
	 */
	public Schema validate() {

		// check each entity descriptor
		for (Entry<Class<?>, EntityDescriptor<?>> entry : entityDescriptors.entrySet()) {
			EntityDescriptor<?> ed = entry.getValue();

			// check pk
			ed.getPrimaryKey();

			// check FKs
			Map<Function1<?, ?>, ForeignKey<?, ?>> fks = ed.getForeignKeys();
			for (Entry<Function1<?, ?>, ForeignKey<?, ?>> fkEntry : fks.entrySet()) {
				ForeignKey<?, ?> fk = fkEntry.getValue();
				entityDescriptors.values().stream()
						.filter((d) -> d.getEntityClass().equals(fk.getForeignEntity())
								|| d.getGraphKey().equals(fk.getForeignEntity()))
						.findAny()
						.orElseThrow(() -> new UnknownFKException(ed.getEntityClass(), fk));
			}
		}

		// good to go
		this.validated = true;
		return this;
	}

}
