package com.joraph;

import java.util.Set;

public class Query {

	private Class<?> entityClass;
	private Set<?> rootObjects;
	private ObjectGraph existingGraph;
	/**
	 * @return the entityClass
	 */
	public Class<?> getEntityClass() {
		return entityClass;
	}
	/**
	 * @param entityClass the entityClass to set
	 */
	public Query withEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
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
	public Query withRootObjects(Set<?> rootObjects) {
		this.rootObjects = rootObjects;
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
