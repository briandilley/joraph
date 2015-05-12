package com.joraph;

@SuppressWarnings("serial")
public class UnconfiguredLoaderException
	extends JoraphException {
	public UnconfiguredLoaderException(Class<?> missingLoaderForEntityClass) {
		super("Missing an EntityLoader for entity class " + missingLoaderForEntityClass.getName());
	}
}
