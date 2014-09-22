package com.joraph;

import java.io.Serializable;
import java.util.List;

public interface EntityLoader<T> {

	/**
	 * Loads the entities with the given ids.
	 * @param is
	 * @return
	 */
	List<T> load(Iterable<Serializable> ids);

}
