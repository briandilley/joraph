package com.joraph.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

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

	public Collection<ForeignKey<?>> getForeignKeys(Class<?> entityClass) {
		EntityDescriptor ed = entityDescriptors.get(entityClass);
		return Collections.unmodifiableCollection(ed.getForeignKeys().values());
	}

	public Collection<ForeignKey<?>> getForeignKeys(Class<?> fromEntityClass, Class<?> toEntityClass) {
		 Collection<ForeignKey<?>> ret = new ArrayList<>();
		 for (ForeignKey<?> fk : getForeignKeys(fromEntityClass)) {
			 if (fk.getForeignEntity().equals(toEntityClass)) {
				 ret.add(fk);
			 }
		 }
		 return Collections.unmodifiableCollection(ret);
	}

	/**
	 * Describes the given entity class.
	 * @param entityClass the class
	 * @return the node
	 */
	public Node describe(Class<?> entityClass) {
		this.assertValidated();
		return new Node(null, this, entityClass, getForeignKeys(entityClass), false);
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
