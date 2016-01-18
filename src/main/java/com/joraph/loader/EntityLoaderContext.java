package com.joraph.loader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.joraph.CollectionUtil;
import com.joraph.JoraphException;
import com.joraph.debug.JoraphDebug;

public class EntityLoaderContext {

	private Map<Class<?>, EntityLoaderDescriptor<?, ?>> loaders = new HashMap<>();

	/**
	 * Creates a builder for adding a loader.
	 */
	public <T> Builder<?, ?, T> addLoader(Class<? super T> entityClass) {
		return new Builder<>(entityClass);
	}

	/**
	 * Adds a loader.
	 */
	public <T> void addLoader(Class<? super T> entityClass, EntityLoaderDescriptor<?, T> loader) {
		this.loaders.put(entityClass, loader);
	}

	/**
	 * Adds a loader.
	 */
	public <T> void addLoader(Class<? super T> entityClass, LoaderFunction<?, T> loader) {
		addLoader(entityClass, new EntityLoaderDescriptor<>(entityClass, null, loader));
	}

	/**
	 * Adds a loader.
	 */
	public <T> void addLoader(Class<? super T> entityClass, Function<Iterable<?>, List<? extends T>> function) {
		addLoader(entityClass, EntityLoaderDescriptor.of(entityClass, function));
	}

	/**
	 * Adds a loader.
	 */
	public <A, T, AA> void addLoader(
			Class<? super T> entityClass,
			BiFunction<AA, Iterable<?>, List<? extends T>> function,
			Class<? super A> argumentClass,
			Function<A, AA> argumentExtractor) {
		addLoader(entityClass, EntityLoaderDescriptor.of(entityClass, function, argumentClass, argumentExtractor));
	}

	/**
	 * Returns the loader for an entity class.
	 * @param entityClass the class
	 * @return the loader
	 */
	@SuppressWarnings("unchecked")
	public <A, T> LoaderFunction<A, T> getLoader(Class<T> entityClass) {
		return Optional.ofNullable(loaders.get(entityClass))
				.map((d)->(EntityLoaderDescriptor<A, T>)d)
				.map(EntityLoaderDescriptor::getLoader)
				.orElseThrow(() -> new UnconfiguredLoaderException(entityClass));
	}

	/**
	 * Loads entities of the given type.
	 * @param entityClass the type
	 * @param ids the ids to load
	 * @return the loaded entities
	 * @throws UnconfiguredLoaderException if a loader wasn't found
	 * @throws JoraphException when a loader throws an exception
	 */
	public <A, T> List<? extends T> load(Class<T> entityClass, Iterable<?> ids) {
		return load(entityClass, null, ids);
	}

	/**
	 * Loads entities of the given type.
	 * @param entityClass the type
	 * @param argumentProvider the argument provider
	 * @param ids the ids to load
	 * @return the loaded entities
	 * @throws UnconfiguredLoaderException if a loader wasn't found
	 * @throws JoraphException when a loader throws an exception
	 */
	public <A, T> List<? extends T> load(Class<T> entityClass, A argumentProvider, Iterable<?> ids) {

		final LoaderFunction<A, T> loader = getLoader(entityClass);
		final List<?> idsToLoad = CollectionUtil.toList(ids);
		final long start = System.currentTimeMillis();

		try {
			List<? extends T> objects = loader.load(argumentProvider, idsToLoad);
			JoraphDebug.addLoaderDebug(
					entityClass,
					System.currentTimeMillis()-start,
					idsToLoad, objects);
			return objects;

		} catch(Throwable t) {
			throw new JoraphException(
					"Error invoking loader: "+loader.toString()
					+" with ids: "+String.join(", ", idsToLoad.stream()
							.filter(Objects::nonNull)
							.map(Object::toString)
							.limit(5)
							.collect(Collectors.toList()))
					+((idsToLoad.size() > 5)
							? "... and "+(idsToLoad.size()-5)+" more"
							: ""),
				t);
		}

	}

	public class Builder<A, AA, T> {

		private Class<? super T> entityClass;
		private BiFunction<AA, Iterable<?>, List<? extends T>> biFunction;
		private Function<Iterable<?>, List<? extends T>> function;
		private Class<? super A> argumentClass;
		private Function<A, AA> argumentExtractor;

		private Builder(Class<? super T> entityClass) {
			this.entityClass = entityClass;
		}

		@SuppressWarnings("unchecked")
		public <A2, AA2> Builder<A2, AA2, T> withArgument(Class<? super A2> argumentClass, Function<A2, AA2> argumentExtractor) {
			this.argumentClass = (Class<? super A>)argumentClass;
			this.argumentExtractor = (Function<A, AA>)argumentExtractor;
			return (Builder<A2, AA2, T>)this;
		}

		public Builder<A, AA, T> withFunction(BiFunction<AA, Iterable<?>, List<? extends T>> function) {
			this.biFunction = function;
			return this;
		}

		public Builder<A, AA, T> withFunction(Function<Iterable<?>, List<? extends T>> function) {
			this.function = function;
			return this;
		}

		public EntityLoaderContext add() {
			if (function!=null) {
				EntityLoaderContext.this.addLoader(entityClass, function);
			} else if (biFunction!=null) {
				EntityLoaderContext.this.addLoader(entityClass, biFunction, argumentClass, argumentExtractor);
			} else {
				throw new IllegalStateException("No loader function specified");
			}
			return EntityLoaderContext.this;
		}

	}
}
