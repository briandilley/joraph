package com.joraph;

public class UnconfiguredLoaderException
	extends JoraphException {
	public UnconfiguredLoaderException(Class<?> missingLoaderForEntityClass) {
		super("Missing an EntityLoader for entity class " + missingLoaderForEntityClass.getName());
	}
}
