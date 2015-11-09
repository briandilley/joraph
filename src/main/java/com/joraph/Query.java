package com.joraph;

import java.util.HashSet;
import java.util.Set;

public class Query {

	private Set<Class<?>> entityClasses;
	private Set<?> rootObjects;
	private ObjectGraph existingGraph;

	/**
	 * @return the entityClasses
	 */
	public Set<Class<?>> getEntityClasses() {
		return entityClasses;
	}

	/**
	 * @param entityClasses the entityClasses to set
	 */
	public Query withEntityClasses(Set<Class<?>> entityClasses) {
		this.entityClasses = entityClasses;
		return this;
	}

	/**
	 * @param entityClass the entityClass to set
	 */
	public Query withEntityClass(Class<?> entityClass) {
		if (this.entityClasses==null || this.entityClasses.isEmpty()) {
			this.entityClasses = new HashSet<Class<?>>();
		}
		this.entityClasses.add(entityClass);
		return this;
	}

	/**
	 * @return the rootObjects
	 */
	public Set<?> getRootObjects() {
		return rootObjects;
	}

	/**
	 * @param rootObjects the rootObjects to set
	 */
	public Query withRootObjects(Iterable<?> rootObjects) {
		this.rootObjects = CollectionUtil.toSet(rootObjects);
		return this;
	}

	/**
	 * @return the existingGraph
	 */
	public ObjectGraph getExistingGraph() {
		return existingGraph;
	}

	/**
	 * @return whether or not there is an existing graph
	 */
	public boolean hasExistingGraph() {
		return existingGraph!=null;
	}

	/**
	 * @param existingGraph the existingGraph to set
	 */
	public Query withExistingGraph(ObjectGraph existingGraph) {
		this.existingGraph = existingGraph;
		return this;
	}

}
