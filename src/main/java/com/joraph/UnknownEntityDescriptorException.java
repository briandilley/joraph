package com.joraph;

public class UnknownEntityDescriptorException
		extends JoraphException {
	public UnknownEntityDescriptorException(Class<?> entityClass) {
		super("Unknown EntityDescriptor for class " + entityClass.getName());
	}
}
