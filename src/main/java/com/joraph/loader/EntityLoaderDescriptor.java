package com.joraph.loader;

public class EntityLoaderDescriptor<A, I, R> {

	private final Class<R> entityClass;
	private final Class<A> argumentClass;
	private final LoaderFunction<A, I, R> loader;

	public EntityLoaderDescriptor(
			Class<R> entityClass,
			Class<A> argumentClass,
			LoaderFunction<A, I, R> loader) {
		this.entityClass = entityClass;
		this.argumentClass = argumentClass;
		this.loader = loader;
	}

	public Class<R> getEntityClass() {
		return entityClass;
	}

	public Class<A> getArgumentClass() {
		return argumentClass;
	}

	public LoaderFunction<A, I, R> getLoader() {
		return loader;
	}

	public boolean requiresAdditionalArguments() {
		return argumentClass != null;
	}

}
