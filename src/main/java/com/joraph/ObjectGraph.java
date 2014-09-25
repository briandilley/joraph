package com.joraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that holds results.
 */
public class ObjectGraph {

	private Map<Class<?>, Map<Object, Object>> results;

	public ObjectGraph() {
		results = new HashMap<>();
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
		return new ArrayList<>(getMap(type).values());
	}

	/**
	 * Returns a list of all items of a given type with
	 * the given ids - sorted in the same way as the ids.
	 * @param type
	 * @param ids
	 * @return
	 */
	public <T> List<T> getList(Class<T> type, Collection<Object> ids) {
		List<T> ret = new ArrayList<>();
		for (Object id : ids) {
			ret.add(get(type, id));
		}
		return ret;
	}

}
