package com.joraph.schema;

import com.joraph.JoraphException;

@SuppressWarnings("serial")
public class PrimaryKeyNullPointerException
		extends JoraphException {
	public PrimaryKeyNullPointerException(Class<?> entityClass) {
		super("Primary key was null for class " + entityClass.getName());
	}
}
