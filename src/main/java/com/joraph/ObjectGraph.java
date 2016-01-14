package com.joraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.joraph.schema.EntityDescriptor;
import com.joraph.schema.Property;
import com.joraph.schema.Schema;

/**
 * A class that holds results.
 */
public class ObjectGraph
		implements Cloneable,
		Iterable<Entry<Class<?>, Map<Object, Object>>> {

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

	@Override
	protected ObjectGraph clone()
			throws CloneNotSupportedException {
		ObjectGraph ret = new ObjectGraph(schema);
		this.copyGraphTo(ret);
		return ret;
	}

	@Override
	public Iterator<Entry<Class<?>, Map<Object, Object>>> iterator() {
		return results.entrySet().iterator();
	}

	/**
	 * Returns the graph type key for the given entity class.
	 * @param entityClass the entity class
	 * @return the key
	 */
	public Class<?> getGraphTypeKey(Class<?> entityClass) {
		return (schema!=null)
				? schema.getGraphTypeKey(entityClass)
				: entityClass;
	}

	/**
	 * Copy this {@link ObjectGraph}'s results to the
	 * given {@link ObjectGraph}.
	 * @param objectGraph the destination
	 */
	public void copyGraphTo(ObjectGraph destinationObjectGraph) {
		for (Entry<Class<?>, Map<Object, Object>> entry : this) {
			entry.getValue().putAll(destinationObjectGraph.results
					.computeIfAbsent(entry.getKey(), __-> new HashMap<>()));
		}
	}

	/**
	 * Copy the given {@link ObjectGraph}'s results to the
	 * this {@link ObjectGraph}.
	 * @param objectGraph the source
	 */
	public void copyGraphFrom(ObjectGraph objectGraph) {
		objectGraph.copyGraphTo(this);
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
		final Class<?> graphTypeKey = getGraphTypeKey(type);
		synchronized (results) {
			if (!results.containsKey(graphTypeKey)) {
				results.put(graphTypeKey, new HashMap<>());
			}
		}
		results.get(graphTypeKey).put(id, value);
	}

	/**
	 * Returns a {@link Function} that delegates to
	 * {@link #has(Class, Object)} for the given type.
	 * @param type the type
	 * @return the function
	 */
	public Predicate<? super Object> hasFunction(final Class<?> type) {
		return (id) -> has(type, id);
	}

	/**
	 * Returns the object of the given type with
	 * the given id.
	 * @param type
	 * @param id
	 * @return
	 */
	public boolean has(Class<?> type, Object id) {
		final Class<?> graphTypeKey = getGraphTypeKey(type);
		Map<Object, Object> map = results.get(graphTypeKey);
		if (map==null) {
			return false;
		}
		return map.containsKey(id);
	}

	/**
	 * Returns a {@link Function} that delegates to
	 * {@link #get(Class, Object)} for the given type.
	 * @param type the type
	 * @return the function
	 */
	public <T> Function<? super Object, T> getFunction(final Class<T> type) {
		return (id) -> get(type, id);
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
		final Class<?> graphTypeKey = getGraphTypeKey(type);
		Map<Object, Object> map = results.get(graphTypeKey);
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
		final Class<?> graphTypeKey = getGraphTypeKey(type);
		Map<Object, Object> map = results.get(graphTypeKey);
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
	 * Returns a {@link Function} that delegates to
	 * {@link #getList(Class, Object)} for the given type.
	 * @param type the type
	 * @return the function
	 */
	public <T, I> Function<Collection<I>, List<T>> getListFunction(final Class<T> type) {
		return (ids) -> getList(type, ids);
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
