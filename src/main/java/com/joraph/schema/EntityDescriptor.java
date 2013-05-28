package com.joraph.schema;

import java.util.HashSet;
import java.util.Set;

public class EntityDescriptor {

	private Class<?> entityClass;
	private Set<ForeignKey> foreignKeys = new HashSet<>();

}
