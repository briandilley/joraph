package com.joraph;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.joraph.loader.EntityLoaderContext;
import com.joraph.schema.Schema;

/**
 * The main class for using Joraph.
 */
public class JoraphContext {

	private Schema schema;
	private EntityLoaderContext loaderContext;
	private ExecutorService executorService;
	private long parallelExecutorDefaultTimeoutMillis = TimeUnit.SECONDS.toMillis(30);

	/**
	 * Creates a context for the given {@link Schema}.
	 * @param schema
	 */
	public JoraphContext(Schema schema) {
		this(schema, 50);
	}

	/**
	 * Creates a context for the given {@link Schema}.
	 * @param schema
	 * @param parallelExecutorCount
	 */
	public JoraphContext(Schema schema, int parallelExecutorCount) {
		this.schema 		= schema;
		this.loaderContext	= new EntityLoaderContext();
		setParallelExecutorCount(parallelExecutorCount);
	}

	/**
	 * Returns the configurd {@link EntityLoaderContext}.
	 * @return the {@link EntityLoaderContext}
	 */
	public EntityLoaderContext getLoaderContext() {
		return loaderContext;
	}

	/**
	 *
	 */
	public ObjectGraph createEmptyGraph() {
		return new ObjectGraph(schema);
	}

	/**
	 *
	 */
	public ObjectGraph execute(Query query) {
		return new ExecutionContext(this, query).execute();
	}

	/**
	 *
	 */
	public <T> ObjectGraph execute(Class<T> entityClass, Collection<T> objects) {
		return execute(new Query()
				.withEntityClass(entityClass)
				.withRootObjects(objects));
	}

	/**
	 *
	 */
	public ObjectGraph execute(Collection<Class<?>> entityClasses, Collection<Object> objects) {
		return execute(new Query()
				.withEntityClasses(entityClasses)
				.withRootObjects(objects));
	}

	/**
	 *
	 */
	public <T> ObjectGraph execute(Class<T> entityClass, Collection<T> objects, ObjectGraph existingGraph) {
		return execute(new Query()
			.withEntityClass(entityClass)
			.withRootObjects(objects)
			.withExistingGraph(existingGraph));
	}

	/**
	 *
	 */
	public ObjectGraph execute(Collection<Class<?>> entityClasses, Collection<Object> objects, ObjectGraph existingGraph) {
		return execute(new Query()
			.withEntityClasses(entityClasses)
			.withRootObjects(objects)
			.withExistingGraph(existingGraph));
	}

	/**
	 *
	 */
	public <T> ObjectGraph execute(Class<T> entityClass, T rootObject) {
		return execute(new Query()
			.withEntityClass(entityClass)
			.withRootObjects(CollectionUtil.asSet(rootObject)));
	}

	/**
	 *
	 */
	public <T> ObjectGraph execute(Class<T> entityClass, T rootObject, ObjectGraph existingGraph) {
		return execute(new Query()
			.withEntityClass(entityClass)
			.withRootObjects(CollectionUtil.asSet(rootObject))
			.withExistingGraph(existingGraph));
	}

	/**
	 *
	 */
	public ObjectGraph executeForRootObject(Object rootObject) {
		assert(rootObject != null);
		final Class<?> entityClass = rootObject.getClass();
		assert(entityClass != null);
		
		return execute(new Query()
			.withEntityClass(entityClass)
			.withRootObjects(CollectionUtil.asSet(rootObject)));
	}

	/**
	 *
	 */
	public ObjectGraph executeForRootObject(Object rootObject, ObjectGraph existingGraph) {
		assert(rootObject != null);
		final Class<?> entityClass = rootObject.getClass();
		assert(entityClass != null);
	
		return execute(new Query()
			.withEntityClass(entityClass)
			.withRootObjects(CollectionUtil.asSet(rootObject))
			.withExistingGraph(existingGraph));
	}

	/**
	 *
	 */
	public <T> List<T> load(Class<T> entityClass, Collection<Object> ids) {
		return loaderContext.load(entityClass, ids);
	}

	/**
	 * Supplements an existing graph with the given data.
	 */
	public <T> ObjectGraph supplement(
			ObjectGraph existingGraph, Class<?> entityType, Collection<Object> ids) {

		List<?> objects = load(entityType, ids);
		if (objects==null || objects.isEmpty()) {
			return existingGraph;
		}

		return execute(new Query()
				.withEntityClass(entityType)
				.withRootObjects(objects.stream()
						.map(Object.class::cast)
						.collect(Collectors.toList()))
				.withExistingGraph(existingGraph));
	}

	public Schema getSchema() {
		return schema;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setParallelExecutorCount(int parallelExecutorCount) {
		ThreadPoolExecutor executorService = new ThreadPoolExecutor(
				parallelExecutorCount, parallelExecutorCount,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
		executorService.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				r.run();
			}
		});
		this.executorService = executorService;
	}

	public long getParallelExecutorDefaultTimeoutMillis() {
		return parallelExecutorDefaultTimeoutMillis;
	}

	public void setParallelExecutorDefaultTimeoutMillis(
			long parallelExecutorDefaultTimeoutMillis) {
		this.parallelExecutorDefaultTimeoutMillis = parallelExecutorDefaultTimeoutMillis;
	}

}
