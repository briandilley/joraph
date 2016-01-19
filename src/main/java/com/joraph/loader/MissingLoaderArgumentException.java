package com.joraph.loader;

import com.joraph.JoraphException;

@SuppressWarnings("serial")
public class MissingLoaderArgumentException
		extends JoraphException {
	public MissingLoaderArgumentException(EntityLoaderDescriptor<?, ?, ?> d) {
		super("Missing an argument of type " + d.getArgumentClass().getName()
				+" for loader configured for entity class " + d.getEntityClass().getName());
	}
}
