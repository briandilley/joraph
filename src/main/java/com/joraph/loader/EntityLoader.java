package com.joraph.loader;

import java.util.List;

/**
 * <p>Identifies a class responsible for loading a series of entities based
 * on the IDs requested.</p>
 * <p>Joraph makes no assumptions about where these entities come from, so
 * it could be a cache or a data store. However it relies on using an
 * {@code Iterable&lt;Serializable&gt;} of IDs in order to enable batching
 * requests.</p>
 * @param <T> the entity type
 */
public interface EntityLoader<T> {

	/**
	 * <p>Loads the entities with the given IDs.</p>
	 * <p>The {@code EntityLoader} may provide less than the number of
	 * requested IDs (if those IDs don't exist in its data store, for
	 * example).</p>
	 * @param ids IDs to load
	 * @return a list of entities which are identified by the provided IDs
	 */
	List<T> load(Iterable<?> ids);

}
