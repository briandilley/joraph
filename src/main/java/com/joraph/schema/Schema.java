package com.joraph.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import com.joraph.JoraphException;

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
	 * @param entityClass the entity class
	 * @return the descriptor for the class, or null if no descriptor exists for it
	 */
	@SuppressWarnings("unchecked")
	public <T> EntityDescriptor<T> getEntityDescriptor(Class<T> entityClass) {
		return (EntityDescriptor<T>)this.entityDescriptors.get(entityClass);
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
	 * Describes the foreign keys configured for a specified class.
	 * @param entityClass the entity class
	 * @return the foreign keys configured for that class
	 */
	public Collection<ForeignKey<?, ?>> describeForeignKeysFrom(Class<?> entityClass) {
		EntityDescriptor<?> ed = getEntityDescriptor(entityClass);
		return Collections.unmodifiableCollection(ed.getForeignKeys().values());
	}

	/**
	 * Describes the foreign keys from one class to another, if any exist.
	 * @param fromEntityClass the from entity class
	 * @param toEntityClass the to entity class
	 * @return all of the foreign keys which exist from one entity class to
	 *         another or an empty collection if no such relationships exist
	 */
	public Collection<ForeignKey<?, ?>> describeForeignKeys(Class<?> fromEntityClass, Class<?> toEntityClass) {
		 Collection<ForeignKey<?, ?>> ret = new ArrayList<>();
		 for (ForeignKey<?, ?> fk : describeForeignKeysFrom(fromEntityClass)) {
			 if (fk.getForeignEntity().equals(toEntityClass)) {
				 ret.add(fk);
			 }
		 }
		 return Collections.unmodifiableCollection(ret);
	}

	/**
	 * Describes the foreign keys coming into a given entity class.
	 * @param toEntityClass the entity class
	 * @return foreign keys that point to an entity class
	 */
	public Collection<ForeignKey<?, ?>> describeForeignKeysTo(Class<?> toEntityClass) {
		 Collection<ForeignKey<?, ?>> ret = new ArrayList<>();
		 for (ForeignKey<?, ?> fk : describeForeignKeys()) {
			 if (fk.getForeignEntity().equals(toEntityClass)) {
				 ret.add(fk);
			 }
		 }
		 return Collections.unmodifiableCollection(ret);
	}

	/**
	 * Describes all of the foreign keys that have been configured.
	 * @return all of the configured foreign keys
	 */
	public Collection<ForeignKey<?, ?>> describeForeignKeys() {
		 Collection<ForeignKey<?, ?>> ret = new ArrayList<>();
		 for (Class<?> fromEntityClass : describeEntities()) {
			 for (ForeignKey<?, ?> fk : describeForeignKeysFrom(fromEntityClass)) {
				 ret.add(fk);
			 }
		 }
		 return Collections.unmodifiableCollection(ret);
	}

	public Graph<Class<?>> graph(Class<?> startClass) {
		Graph<Class<?>> ret = new Graph<>(CLASS_COMPARATOR);
		graph(describe(startClass), ret);
		return ret;
	}

	private void graph(Node node, Graph<Class<?>> graph) {
		if (node.isCircular()) {
			throw new JoraphException("Circular dependency detected on "+node.getEntityClass());
		}
		graph.addEntity(node.getEntityClass());
		for (ForeignKey<?, ?> fk : node.getForeignKeys()) {
			graph.addEdge(node.getEntityClass(), fk.getForeignEntity());
			graph(node.child(fk), graph);
		}
	}

	/**
	 * Describes the given entity class.
	 * @param entityClass the class
	 * @return the node
	 */
	public Node describe(Class<?> entityClass) {
		this.assertValidated();
		return new Node(null, this, entityClass, describeForeignKeysFrom(entityClass), false);
	}

	/**
	 * Describes the given entity class.
	 * @param entityClass the class
	 * @return the node
	 */
	public Set<Class<?>> describeEntities() {
		this.assertValidated();
		return Collections.unmodifiableSet(entityDescriptors.keySet());
	}

	/**
	 * Validates the {@link Schema}.
	 */
	public void validate() {

		// check each entity descriptor
		for (Entry<Class<?>, EntityDescriptor<?>> entry : entityDescriptors.entrySet()) {
			EntityDescriptor<?> ed = entry.getValue();

			// check pk
			if (ed.getPrimaryKey()==null) {
				throw new MissingPrimaryKeyException(ed);
			}

			// check FKs
			Map<PropertyDescriptorChain<?, ?>, ForeignKey<?, ?>> fks = ed.getForeignKeys();
			for (Entry<PropertyDescriptorChain<?, ?>, ForeignKey<?, ?>> fkEntry : fks.entrySet()) {
				ForeignKey<?, ?> fk = fkEntry.getValue();
				if (!entityDescriptors.containsKey(fk.getForeignEntity())) {
					throw new UnknownFKException(ed.getEntityClass(), fk);
				}
			}
		}

		// good to go
		this.validated = true;
	}

}
