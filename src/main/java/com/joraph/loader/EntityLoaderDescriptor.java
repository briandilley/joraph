package com.joraph.loader;

import java.util.HashSet;
import java.util.Set;

public class EntityLoaderDescriptor<T> {

	private final Set<Class<? extends T>> classes = new HashSet<>();
	private EntityLoader<? extends T> loader;

	public EntityLoaderDescriptor<T> addClass(Class<? extends T> clazz) {
		this.classes.add(clazz);
		return this;
	}

	public EntityLoaderDescriptor<T> setLoader(EntityLoader<? extends T> loader) {
		this.loader = loader;
		return this;
	}

	public boolean canLoad(Class<? extends T> clazz) {
		return classes.contains(clazz);
	}

	public EntityLoader<? extends T> getLoader() {
		return loader;
	}

}
