package com.joraph.loader;

import static com.joraph.loader.LoaderFunction.ofArrayArray;
import static com.joraph.loader.LoaderFunction.ofArrayItr;
import static com.joraph.loader.LoaderFunction.ofItrArray;
import static com.joraph.loader.LoaderFunction.ofItrItr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.joraph.CollectionUtil;
import com.joraph.JoraphException;
import com.joraph.debug.JoraphDebug;

public class EntityLoaderContext {

	private Map<Class<?>, EntityLoaderDescriptor<?, ?, ?>> loaders = new HashMap<>();

	/**
	 * Adds a loader.
	 */
	public <T> EntityLoaderDescriptorBuilder<?, ?, ?, T> addLoader(Class<T> entityClass) {
		return new EntityLoaderDescriptorBuilder<>(this, entityClass);
	}

	/**
	 * Adds a loader.
	 */
	public <T> EntityLoaderContext addLoader(
			Class<? super T> entityClass,
			EntityLoaderDescriptor<?, ?, ?> loader) {
		this.loaders.put(entityClass, loader);
		return this;
	}

	/**
	 * Adds a loader.
	 */
	public <A, I, R> EntityLoaderContext addLoader(
			Class<R> entityClass,
			LoaderFunction<A, I, R> loader) {
		return addLoader(entityClass, new EntityLoaderDescriptor<>(entityClass, null, loader));
	}

	/**
	 * Adds a loader.
	 */
	public <A, I, R> EntityLoaderContext addLoader(
			Class<R> entityClass,
			Class<A> argumentClass,
			LoaderFunction<A, I, R> loader) {
		return addLoader(entityClass, new EntityLoaderDescriptor<>(entityClass, argumentClass, loader));
	}

	/**
	 * Returns the loader for an entity class.
	 * @param entityClass the class
	 * @return the loader
	 */
	@SuppressWarnings("unchecked")
	public <A, I, R> EntityLoaderDescriptor<A, I, R> getLoader(Class<R> entityClass) {
		return Optional.ofNullable(loaders.get(entityClass))
				.map((d)->(EntityLoaderDescriptor<A, I, R>)d)
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
	public <A, I, R> List<R> load(Class<R> entityClass, Iterable<I> ids) {
		return load(entityClass, null, ids);
	}

	/**
	 * Loads entities of the given type.
	 * @param entityClass the type
	 * @param argumentProvider the argument provider
	 * @param ids the ids to load
	 * @return the loaded entities
	 * @throws UnconfiguredLoaderException if a loader wasn't found
	 * @throws MissingLoaderArgumentException if a loader expected an argument that wasn't provided
	 * @throws JoraphException when a loader throws an exception
	 */
	public <A, I, R> List<R> load(Class<R> entityClass, List<Object> arguments, Iterable<I> ids)
			throws UnconfiguredLoaderException,
			MissingLoaderArgumentException,
			JoraphException {

		final EntityLoaderDescriptor<A, I, R> loader = getLoader(entityClass);
		if (loader.requiresAdditionalArguments()
				&& (arguments == null || arguments.isEmpty())) {
			throw new MissingLoaderArgumentException(loader);
		}

		final A argument;
		if (loader.requiresAdditionalArguments()) {
			argument = arguments.stream()
					.filter(loader.getArgumentClass()::isInstance)
					.map(loader.getArgumentClass()::cast)
					.findFirst()
					.orElseThrow(() -> new MissingLoaderArgumentException(loader));
		} else {
			argument = null;
		}

		final List<I> idsToLoad = CollectionUtil.toList(ids);

		try {
			final long start = System.currentTimeMillis();
			List<R> objects = loader.getLoader().load(argument, idsToLoad);
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



	@SuppressWarnings("unchecked")
	public static class EntityLoaderDescriptorBuilder<A, AA, I, R> {

		private EntityLoaderContext context;
		
		private Class<R> entityClass;
		private Class<A> argumentClass;
		private Function<A, AA> argumentExtractor;
		private LoaderFunction<A, I, R> loader;

		private EntityLoaderDescriptorBuilder(EntityLoaderContext context, Class<R> entityClass) {
			this.context = context;
			this.entityClass = entityClass;
		}

		public EntityLoaderContext add() {
			if (argumentClass!=null) {
				return context.addLoader(entityClass, argumentClass, loader);
			} else {
				return context.addLoader(entityClass, loader);
			}
		}

		public <A2, AA2> EntityLoaderDescriptorBuilder<A2, AA2, I, R> withArgument(Class<A2> argumentClass, Function<A2, AA2> argumentExtractor) {
			EntityLoaderDescriptorBuilder<A2, AA2, I, R> ret = (EntityLoaderDescriptorBuilder<A2, AA2, I, R>)this;
			ret.argumentClass = argumentClass;
			ret.argumentExtractor = argumentExtractor;
			return ret;
		}

		public EntityLoaderDescriptorBuilder<A, AA, I, R> withLoader(LoaderFunction<A, I, R> loader) {
			this.loader = loader;
			return this;
		}



		public <I2> EntityLoaderDescriptorBuilder<A, AA, I2, R>
				withArrayItrFunction(Function<I2[], Iterable<R>> function, IntFunction<I2[]> arraySupplier) {
			EntityLoaderDescriptorBuilder<A, AA, I2, R> ret = (EntityLoaderDescriptorBuilder<A, AA, I2, R>)this;
			return ret.withLoader(ofArrayItr(function, arraySupplier));
		}

		public <I2> EntityLoaderDescriptorBuilder<A, AA, I2, R>
				withArrayArrayFunction(Function<I2[], R[]> function, IntFunction<I2[]> arraySupplier) {
			EntityLoaderDescriptorBuilder<A, AA, I2, R> ret = (EntityLoaderDescriptorBuilder<A, AA, I2, R>)this;
			return ret.withLoader(ofArrayArray(function, arraySupplier));
		}

		public <I2, C extends Collection<I2>> EntityLoaderDescriptorBuilder<A, AA, I2, R>
				withItrItrFunction(Function<C, Iterable<R>> function, Supplier<C> supplier) {
			EntityLoaderDescriptorBuilder<A, AA, I2, R> ret = (EntityLoaderDescriptorBuilder<A, AA, I2, R>)this;
			return ret.withLoader(ofItrItr(function, supplier));
		}

		public <I2, C extends Collection<I2>> EntityLoaderDescriptorBuilder<A, AA, I2, R>
				withItrArrayFunction(Function<C, R[]> function, Supplier<C> supplier) {
			EntityLoaderDescriptorBuilder<A, AA, I2, R> ret = (EntityLoaderDescriptorBuilder<A, AA, I2, R>)this;
			return ret.withLoader(ofItrArray(function, supplier));
		}

		public <I2> EntityLoaderDescriptorBuilder<A, AA, I2, R>
				withSetFunction(Function<Set<I2>, Iterable<R>> function) {
			return withItrItrFunction(function, HashSet::new);
		}

		public <I2> EntityLoaderDescriptorBuilder<A, AA, I2, R>
				withListFunction(Function<List<I2>, Iterable<R>> function) {
			return withItrItrFunction(function, ArrayList::new);
		}


	
		public <I2> EntityLoaderDescriptorBuilder<A, AA, I2, R>
				withArrayItrBiFunction(BiFunction<AA, I2[], Iterable<R>> function, IntFunction<I2[]> arraySupplier) {
			EntityLoaderDescriptorBuilder<A, AA, I2, R> ret = (EntityLoaderDescriptorBuilder<A, AA, I2, R>)this;
			return ret.withLoader(ofArrayItr(function, arraySupplier, argumentExtractor));
		}

		public <I2> EntityLoaderDescriptorBuilder<A, AA, I2, R>
				withArrayArrayBiFunction(BiFunction<AA, I2[], R[]> function, IntFunction<I2[]> arraySupplier) {
			EntityLoaderDescriptorBuilder<A, AA, I2, R> ret = (EntityLoaderDescriptorBuilder<A, AA, I2, R>)this;
			return ret.withLoader(ofArrayArray(function, arraySupplier, argumentExtractor));
		}

		public <I2, C extends Collection<I2>> EntityLoaderDescriptorBuilder<A, AA, I2, R>
				withItrItrBiFunction(BiFunction<AA, C, Iterable<R>> function, Supplier<C> supplier) {
			EntityLoaderDescriptorBuilder<A, AA, I2, R> ret = (EntityLoaderDescriptorBuilder<A, AA, I2, R>)this;
			return ret.withLoader(ofItrItr(function, supplier, argumentExtractor));
		}

		public <I2, C extends Collection<I2>> EntityLoaderDescriptorBuilder<A, AA, I2, R>
				withItrArrayBiFunction(BiFunction<AA, C, R[]> function, Supplier<C> supplier) {
			EntityLoaderDescriptorBuilder<A, AA, I2, R> ret = (EntityLoaderDescriptorBuilder<A, AA, I2, R>)this;
			return ret.withLoader(ofItrArray(function, supplier, argumentExtractor));
		}

		public <I2> EntityLoaderDescriptorBuilder<A, AA, I2, R>
				withSetBiFunction(BiFunction<AA, Set<I2>, Iterable<R>> function) {
			return withItrItrBiFunction(function, HashSet::new);
		}

		public <I2> EntityLoaderDescriptorBuilder<A, AA, I2, R>
				withListBiFunction(BiFunction<AA, List<I2>, Iterable<R>> function) {
			return withItrItrBiFunction(function, ArrayList::new);
		}



	}

}
