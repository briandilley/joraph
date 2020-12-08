package com.joraph.schema;

import com.joraph.JoraphException;

/**
 * An exception which occurs when the foreign key is circular.
 */
@SuppressWarnings("serial")
public class CircularDependencyException
	extends JoraphException {

	private final Class<?> entityClass;

	public CircularDependencyException(Class<?> entityClass) {
		this(entityClass, entityClass.getName());
	}

	public CircularDependencyException(Class<?> entityClass, String message) {
		super(message);
		this.entityClass = entityClass;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

}
