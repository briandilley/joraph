package com.joraph.schema;

import com.joraph.JoraphException;

@SuppressWarnings("serial")
public class UnknownEntityDescriptorException
		extends JoraphException {
	public UnknownEntityDescriptorException(Class<?> entityClass) {
		super("Unknown EntityDescriptor for class " + entityClass.getName());
	}
}
