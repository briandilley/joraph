package com.joraph.schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Graph {

	private Set<Class<?>> entities = new HashSet<>();
	private Map<Class<?>, Set<Class<?>>> outgoingEdge = new HashMap<>();
	private Map<Class<?>, Set<Class<?>>> incomingEdge = new HashMap<>();

	public Set<Class<?>> getEntitiesWithoutIncomingEdges() {
		Set<Class<?>> ret = new HashSet<>();
		for (Class<?> entity : entities) {
			if (!incomingEdge.containsKey(entity)) {
				ret.add(entity);
			}
		}
		return ret;
	}

	public Set<Class<?>> getEntities() {
		return Collections.unmodifiableSet(entities);
	}

	public void addEntity(Class<?> entity) {
		entities.add(entity);
	}

	public void removeEntity(Class<?> entity) {
		entities.remove(entity);
		outgoingEdge.remove(entity);
		incomingEdge.remove(entity);
		for (Class<?> key : incomingEdge.keySet()) {
			incomingEdge.get(key).remove(entity);
		}
		for (Class<?> key : outgoingEdge.keySet()) {
			outgoingEdge.get(key).remove(entity);
		}
	}

	public void addEdge(Class<?> from, Class<?> to) {
		addEntity(from);
		addEntity(to);
		if (!outgoingEdge.containsKey(from)) {
			outgoingEdge.put(from, new HashSet<Class<?>>());
		}
		if (!incomingEdge.containsKey(to)) {
			incomingEdge.put(to, new HashSet<Class<?>>());
		}
		outgoingEdge.get(from).add(to);
		incomingEdge.get(to).add(from);
	}

	public void removeEdge(Class<?> from, Class<?> to) {
		if (outgoingEdge.containsKey(from)) {
			outgoingEdge.get(from).remove(to);
		}
		if (incomingEdge.containsKey(to)) {
			incomingEdge.get(to).remove(from);
		}
	}

	public boolean hasEdge(Class<?> from, Class<?> to) {
		return outgoingEdge.containsKey(from)
			&& outgoingEdge.get(from).contains(to);
	}

	public boolean hasIncomingEdge(Class<?> to) {
		return incomingEdge.containsKey(to)
			&& !incomingEdge.get(to).isEmpty();
	}

}
