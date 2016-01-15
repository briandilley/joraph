package com.joraph.schema;

import java.util.HashSet;
import java.util.Optional;

public class EntityDescriptorCollection
		extends HashSet<EntityDescriptor<?>> {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public <T> Optional<EntityDescriptor<T>> findFirstByEntityClass(Class<T> entityClass) {
		return stream()
				.filter((entityDescriptor) -> entityDescriptor.getEntityClass().equals(entityClass))
				.map((entityDescriptor) -> (EntityDescriptor<T>) entityDescriptor)
				.findFirst();
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<EntityDescriptor<T>> findFirstByGraphKey(Class<T> graphKey) {
		return stream()
				.filter((entityDescriptor) -> entityDescriptor.getGraphKey().equals(graphKey))
				.map((entityDescriptor) -> (EntityDescriptor<T>) entityDescriptor)
				.findFirst();
	}

}
