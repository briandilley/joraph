package com.joraph.loader;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class EntityLoaderDescriptor<A, T> {

	private final Class<? super T> entityClass;
	private final Class<? super A> argumentClass;
	private final LoaderFunction<A, T> loader;

	public EntityLoaderDescriptor(Class<? super T> entityClass, Class<? super A> argumentClass, LoaderFunction<A, T> loader) {
		this.entityClass = entityClass;
		this.argumentClass = argumentClass;
		this.loader = loader;
	}

	public Class<? super T> getEntityClass() {
		return entityClass;
	}

	public Class<? super A> getArgumentClass() {
		return argumentClass;
	}

	public LoaderFunction<A, T> getLoader() {
		return loader;
	}

	public static <T> EntityLoaderDescriptor<?, T> of(
			Class<? super T> entityClass,
			Function<Iterable<?>, List<? extends T>> function) {
		return new EntityLoaderDescriptor<>(entityClass, null, (arguments, ids) -> function.apply(ids));
	}

	public static <A, T, AA> EntityLoaderDescriptor<A, T> of(
			Class<? super T> entityClass,
			BiFunction<AA, Iterable<?>, List<? extends T>> function,
			Class<? super A> argumentClass,
			Function<A, AA> argumentExtractor) {
		return new EntityLoaderDescriptor<A, T>(entityClass, argumentClass, (arguments, ids) ->
				function.apply(arguments!=null
						? argumentExtractor.apply(arguments)
						: null,
					ids));
	}

}
