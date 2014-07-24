package com.joraph;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface EntityLoader<T> {

	/**
	 * Loads the entities with the given ids.
	 * @param is
	 * @return
	 */
	List<T> load(Collection<Serializable> ids);

	/**
	 * Returns the given entity's id.
	 * @param entity
	 * @return
	 */
	Serializable getId(T entity);

}
