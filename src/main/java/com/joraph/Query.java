package com.joraph;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Query {

	private Set<Class<?>> entityClasses = new HashSet<>();
	private Set<Object> rootObjects 	= new HashSet<>();
	private List<Object> arguments 		= new ArrayList<>();
	private ObjectGraph existingGraph;

	public Query() {
		
	}

	public Query(Class<?>... entityClasses) {
		withEntityClass(entityClasses);
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
	public Query withEntityClasses(Collection<Class<?>> entityClasses) {
		this.entityClasses.addAll(entityClasses);
		return this;
	}

	/**
	 * @param entityClass the entityClass to set
	 */
	public Query withEntityClass(Class<?>... entityClasses) {
		return withEntityClasses(Arrays.stream(entityClasses)
				.collect(Collectors.toSet()));
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
	public Set<Object> getRootObjects() {
		return rootObjects;
	}

	/**
	 * @param rootObjects the rootObjects to set
	 */
	public Query withRootEntities(Collection<?> rootEntities) {
		return withRootEntity(rootEntities.stream()
				.toArray(Object[]::new));
	}

	/**
	 * @param rootObjects the rootObjects to set
	 */
	public Query withRootEntity(Object... rootEntities) {
		return withRootObjects(asList(rootEntities))
				.withEntityClasses(Arrays.stream(rootEntities)
						.map(Object::getClass)
						.collect(Collectors.toList()));
	}

	/**
	 * @param rootObjects the rootObjects to set
	 */
	public Query withRootObject(Object... rootObjects) {
		return withRootObjects(asList(rootObjects));
	}

	/**
	 * @param rootObjects the rootObjects to set
	 */
	public Query withRootObjects(Collection<?> rootObjects) {
		this.rootObjects.addAll(rootObjects);
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
	public Query withArguments(Collection<?> arguments) {
		this.arguments.addAll(arguments);
		return this;
	}

	/**
	 * @param arguments
	 */
	public Query withArgument(Object... arguments) {
		return withArguments(asList(arguments));
	}

}
