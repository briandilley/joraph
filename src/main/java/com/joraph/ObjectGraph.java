package com.joraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.joraph.schema.EntityDescriptor;
import com.joraph.schema.Property;
import com.joraph.schema.Schema;

/**
 * A class that holds results.
 */
public class ObjectGraph {

	private Map<Class<?>, Map<Object, Object>> results;
	private Schema schema;

	public ObjectGraph() {
		this.results = new HashMap<>();
		this.schema = null;
	}

	public ObjectGraph(Schema schema) {
		this.results = new HashMap<>();
		this.schema = schema;
	}

	/**
	 * Returns all of the ids for the given type.
	 * @param type the type
	 * @return the ids
	 */
	@SuppressWarnings("unchecked")
	public <T> Set<T> getIds(Class<?> type) {
		assert( schema != null );
		EntityDescriptor<?> descriptor = schema.getEntityDescriptor(type);
		assert( descriptor != null );
		Property<?, ?> pk = descriptor.getPrimaryKey();
		assert( pk != null );

		Set<T> ret = new HashSet<>();
		for (Object o : getList(type)) {
			ret.add((T)pk.read(o));
		}
		return ret;
	}

	/**
	 * Adds a result.
	 * @param type
	 * @param id
	 * @param value
	 */
	public void addResult(Class<?> type, Object id, Object value) {
		synchronized (results) {
			if (!results.containsKey(type)) {
				results.put(type, new HashMap<>());
			}
		}
		results.get(type).put(id, value);
	}

	/**
	 * Returns the object of the given type with
	 * the given id.
	 * @param type
	 * @param id
	 * @return
	 */
	public boolean has(Class<?> type, Object id) {
		Map<Object, Object> map = results.get(type);
		if (map==null) {
			return false;
		}
		return map.containsKey(id);
	}

	/**
	 * Returns the object of the given type with
	 * the given id.
	 * @param type
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> type, Object id) {
		Map<Object, Object> map = results.get(type);
		if (map==null) {
			return null;
		}
		return (T)map.get(id);
	}

	/**
	 * Returns a map of all items of a given type.
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Map<Object, T> getMap(Class<T> type) {
		Map<Object, Object> map = results.get(type);
		return map!=null
				? (Map<Object, T>)map
				: new HashMap<Object, T>();
	}

	/**
	 * Returns a list of all items of a given type.
	 * @param type
	 * @return
	 */
	public <T> List<T> getList(Class<T> type) {
		return stream(type).collect(Collectors.toList());
	}

	/**
	 * Returns a list of all items of a given type.
	 * @param type
	 * @return
	 */
	public <T> Stream<T> stream(Class<T> type) {
		return getMap(type).values().stream();
	}

	/**
	 * Returns a list of all items of a given type.
	 * @param type
	 * @return
	 */
	public Stream<Object> streamIds(Class<?> type) {
		return getMap(type).keySet().stream();
	}

	/**
	 * Returns a list of all items of a given type.
	 * @param type
	 * @return
	 */
	public <I> Stream<I> streamIds(Class<?> type, Class<I> idClass) {
		return getMap(type).keySet().stream()
				.map(idClass::cast);
	}

	/**
	 * Returns a list of all items of a given type with
	 * the given ids - sorted in the same way as the ids.
	 * @param type
	 * @param ids
	 * @return
	 */
	public <T, I> List<T> getList(Class<T> type, Collection<I> ids) {
		List<T> ret = new ArrayList<>();
		for (Object id : ids) {
			ret.add(get(type, id));
		}
		return ret;
	}

	/**
	 * Returns the results map.
	 * @return the results
	 */
	public Map<Class<?>, Map<Object, Object>> getResults() {
		return results;
	}

}
