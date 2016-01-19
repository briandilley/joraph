package com.joraph;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Query {

	private Set<Class<?>> entityClasses;
	private Set<?> rootObjects;
	private ObjectGraph existingGraph;
	private List<Object> arguments = new ArrayList<>();

	public Query() {
		
	}

	public Query(Class<?>... entityClasses) {
		withEntityClasses(Arrays.stream(entityClasses)
				.collect(Collectors.toSet()));
	}


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
	 * @return the argumentProvider
	 */
	public List<Object> getArguments() {
		return arguments;
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
	public Query withRootObject(Object... rootObjects) {
		return withRootObjects(Arrays.stream(rootObjects)
				.collect(Collectors.toSet()));
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

	/**
	 * @param arguments
	 */
	public Query withArguments(List<Object> arguments) {
		this.arguments = new ArrayList<>(arguments);
		return this;
	}

	/**
	 * @param arguments
	 */
	public Query withArgument(Object argument) {
		this.arguments = new ArrayList<>();
		this.arguments.add(argument);
		return this;
	}

	/**
	 * @param arguments
	 */
	public Query addArguments(List<Object> arguments) {
		this.arguments.addAll(arguments);
		return this;
	}

	/**
	 * @param arguments
	 */
	public Query addArgument(Object argument) {
		this.arguments.add(argument);
		return this;
	}

	/**
	 * @param arguments
	 */
	public Query addArguments(Object... arguments) {
		this.arguments.addAll(asList(arguments));
		return this;
	}

}
