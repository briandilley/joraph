package com.joraph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that holds results.
 */
public class Results {

	private Map<Class<?>, Map<Serializable, Object>> results;

	public Results() {
		results = new HashMap<>();
	}

	/**
	 * Adds a result.
	 * @param type
	 * @param id
	 * @param value
	 */
	public void addResult(Class<?> type, Serializable id, Object value) {
		synchronized (results) {
			if (!results.containsKey(type)) {
				results.put(type, new HashMap<Serializable, Object>());
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
	public <T> T get(Class<?> type, Serializable id) {
		Map<Serializable, Object> map = results.get(type);
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
	public <T> Map<Serializable, T> getMap(Class<?> type) {
		Map<Serializable, Object> map = results.get(type);
		return map!=null
				? (Map<Serializable, T>)map
				: new HashMap<Serializable, T>();
	}

	/**
	 * Returns a list of all items of a given type.
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getList(Class<?> type) {
		return new ArrayList<T>((Collection<T>)getMap(type).values());
	}

	/**
	 * Returns a list of all items of a given type with
	 * the given ids - sorted in the same way as the ids.
	 * @param type
	 * @param ids
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getList(Class<?> type, Collection<Serializable> ids) {
		List<T> ret = new ArrayList<T>();
		for (Serializable id : ids) {
			ret.add((T)get(type, id));
		}
		return ret;
	}

}
