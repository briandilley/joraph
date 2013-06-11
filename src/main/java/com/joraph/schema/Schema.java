package com.joraph.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class Schema {

	private boolean validated = false;
	private Map<Class<?>, EntityDescriptor> entityDescriptors = new HashMap<>();

	private void dirty() {
		this.validated = false;
	}

	public boolean isValidated() {
		return validated;
	}

	public void assertValidated() {
		if (!isValidated()) {
			throw new IllegalStateException("Schema not validated");
		}
	}

	public void addEntityDescriptor(EntityDescriptor entityDescriptor) {
		this.dirty();
		this.entityDescriptors.put(entityDescriptor.getEntityClass(), entityDescriptor);
	}

	public EntityDescriptor addEntityDescriptor(Class<?> entityClass) {
		this.dirty();
		EntityDescriptor ret = new EntityDescriptor(entityClass);
		addEntityDescriptor(ret);
		return ret;
	}

	public EntityDescriptor addEntityDescriptor(Class<?> entityClass, String primaryKey) {
		this.dirty();
		EntityDescriptor ret = new EntityDescriptor(entityClass);
		addEntityDescriptor(ret);
		return ret;
	}

	public Collection<ForeignKey<?>> describeForeignKeysFrom(Class<?> entityClass) {
		EntityDescriptor ed = entityDescriptors.get(entityClass);
		return Collections.unmodifiableCollection(ed.getForeignKeys().values());
	}

	public Collection<ForeignKey<?>> describeForeignKeys(Class<?> fromEntityClass, Class<?> toEntityClass) {
		 Collection<ForeignKey<?>> ret = new ArrayList<>();
		 for (ForeignKey<?> fk : describeForeignKeysFrom(fromEntityClass)) {
			 if (fk.getForeignEntity().equals(toEntityClass)) {
				 ret.add(fk);
			 }
		 }
		 return Collections.unmodifiableCollection(ret);
	}

	public Collection<ForeignKey<?>> describeForeignKeysTo(Class<?> toEntityClass) {
		 Collection<ForeignKey<?>> ret = new ArrayList<>();
		 for (ForeignKey<?> fk : describeForeignKeys()) {
			 if (fk.getForeignEntity().equals(toEntityClass)) {
				 ret.add(fk);
			 }
		 }
		 return Collections.unmodifiableCollection(ret);
	}

	public Collection<ForeignKey<?>> describeForeignKeys() {
		 Collection<ForeignKey<?>> ret = new ArrayList<>();
		 for (Class<?> fromEntityClass : describeEntities()) {
			 for (ForeignKey<?> fk : describeForeignKeysFrom(fromEntityClass)) {
				 ret.add(fk);
			 }
		 }
		 return Collections.unmodifiableCollection(ret);
	}

	public Graph graph(Class<?> startClass) {
		Graph ret = new Graph();
		graph(describe(startClass), ret);
		return ret;
	}

	private void graph(Node node, Graph graph) {
		graph.addEntity(node.getEntityClass());
		for (ForeignKey<?> fk : node.getForeignKeys()) {
			graph.addEdge(node.getEntityClass(), fk.getForeignEntity());
			graph(node.child(fk.getName()), graph);
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
		for (Entry<Class<?>, EntityDescriptor> entry : entityDescriptors.entrySet()) {
			EntityDescriptor ed = entry.getValue();

			// check pk
			if (ed.getPrimaryKey()==null) {
				throw new MissingPrimaryKeyException(ed);
			}

			// check FKs
			for (Entry<String, ForeignKey<?>> fkEntry : ed.getForeignKeys().entrySet()) {
				ForeignKey<?> fk = fkEntry.getValue();
				if (!entityDescriptors.containsKey(fk.getForeignEntity())) {
					throw new UnknownFKException(ed.getEntityClass(), fk);
				}
			}
		}

		// good to go
		this.validated = true;
	}

}
