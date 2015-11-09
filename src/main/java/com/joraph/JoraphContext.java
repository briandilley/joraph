package com.joraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.joraph.plan.ExecutionPlan;
import com.joraph.plan.ExecutionPlanner;
import com.joraph.schema.Schema;

/**
 * Derives a series of {@link com.joraph.plan.Operation} from a
 * {@link com.joraph.schema.Schema}.
 */
public class JoraphContext {

	private Schema schema;
	private ExecutionPlanner planner;
	private Map<Set<Class<?>>, ExecutionPlan> cachedPlans;
	private Map<Class<?>, EntityLoader<?>> loaders;
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
		this.planner		= new ExecutionPlanner(this);
		this.cachedPlans	= new HashMap<>();
		this.loaders		= new HashMap<>();
		setParallelExecutorCount(parallelExecutorCount);
	}

	/**
	 * Adds a loader.
	 * @param entityClass the class
	 * @param loader the loader
	 */
	public void addLoader(Class<?> entityClass, EntityLoader<?> loader) {
		this.loaders.put(entityClass, loader);
	}

	/**
	 * Creates and returns an empty {@link ObjectGraph}.
	 * @return the graph
	 */
	public ObjectGraph createEmptyGraph() {
		return new ObjectGraph(schema);
	}

	/**
	 * 
	 * @param entityClass the entity class
	 * @param objects
	 * @return
	 */
	public <T> ObjectGraph execute(Query query) {
		return new ExecutionContext(this, query).execute();
	}

	/**
	 * 
	 * @param entityClass the entity class
	 * @param objects
	 * @return
	 */
	public <T> ObjectGraph execute(Class<T> entityClass, Iterable<T> objects) {
		return execute(new Query()
				.withEntityClass(entityClass)
				.withRootObjects(objects));
	}

	/**
	 * 
	 * @param entityClasses the entity classes
	 * @param objects
	 * @return
	 */
	public ObjectGraph execute(Set<Class<?>> entityClasses, Iterable<Object> objects) {
		return execute(new Query()
				.withEntityClasses(entityClasses)
				.withRootObjects(objects));
	}

	/**
	 * 
	 * @param entityClass the entity class
	 * @param objects
	 * @return
	 */
	public <T> ObjectGraph execute(Class<T> entityClass, Iterable<T> objects, ObjectGraph existingGraph) {
		return execute(new Query()
			.withEntityClass(entityClass)
			.withRootObjects(objects)
			.withExistingGraph(existingGraph));
	}

	/**
	 * 
	 * @param entityClass the entity class
	 * @param objects
	 * @return
	 */
	public ObjectGraph execute(Set<Class<?>> entityClasses, Iterable<Object> objects, ObjectGraph existingGraph) {
		return execute(new Query()
			.withEntityClasses(entityClasses)
			.withRootObjects(objects)
			.withExistingGraph(existingGraph));
	}

	/**
	 * Executes and retrieves an object graph using the appropriate {@link EntityLoader}s
	 * starting with the specified {@code rootObject}.
	 * @param entityClass the entity class
	 * @param rootObject the root object to create the graph from
	 * @param <T> the entity class
	 * @return an object graph derived from the relationships defined in in the schema and
	 * associated with the rootObject
	 */
	public <T> ObjectGraph execute(Class<T> entityClass, T rootObject) {
		return execute(new Query()
			.withEntityClass(entityClass)
			.withRootObjects(CollectionUtil.asSet(rootObject)));
	}

	/**
	 * Executes and retrieves an object graph using the appropriate {@link EntityLoader}s
	 * starting with the specified {@code rootObject}.
	 * @param entityClass the entity class
	 * @param rootObject the root object to create the graph from
	 * @param <T> the entity class
	 * @return an object graph derived from the relationships defined in in the schema and
	 * associated with the rootObject
	 */
	public <T> ObjectGraph execute(Class<T> entityClass, T rootObject, ObjectGraph existingGraph) {
		return execute(new Query()
			.withEntityClass(entityClass)
			.withRootObjects(CollectionUtil.asSet(rootObject))
			.withExistingGraph(existingGraph));
	}

	/**
	 * <p>Executes and retrieves an object graph using the appropriate {@link EntityLoader}s
	 * deriving the {@code entityClass} from the class of the passed in {@code rootObject}.</p>
	 * <p>Assumes that the class of {@code rootObject} is a defined class within the schema.</p>
	 * @param rootObject the root object to create the graph from
	 * @param <T> the entity class
	 * @return an object graph derived from the relationships defined in in the schema and
	 * associated with the rootObject
	 */
	public ObjectGraph executeForObject(Object rootObject) {
		assert(rootObject != null);
		final Class<?> entityClass = rootObject.getClass();
		assert(entityClass != null);
		
		return execute(new Query()
			.withEntityClass(entityClass)
			.withRootObjects(CollectionUtil.asSet(rootObject)));
	}

	/**
	 * <p>Executes and retrieves an object graph using the appropriate {@link EntityLoader}s
	 * deriving the {@code entityClasses} from the classes of the passed in {@code rootObjects}.</p>
	 * <p>Assumes that the classes of {@code rootObjects} are defined classes within the schema.</p>
	 * @param rootObjects the root object to create the graph from
	 * @param <T> the entity class
	 * @return an object graph derived from the relationships defined in in the schema and
	 * associated with the rootObject
	 */
	public ObjectGraph executeForObjects(Iterable<?> rootObjects) {
		assert(rootObjects != null);
		final Set<Class<?>> entityClasses = new HashSet<>();
		for (Object rootObject : rootObjects) {
			entityClasses.add(rootObject.getClass());
		}
		
		return execute(new Query()
			.withEntityClasses(entityClasses)
			.withRootObjects(rootObjects));
	}

	/**
	 * <p>Executes and retrieves an object graph using the appropriate {@link EntityLoader}s
	 * deriving the {@code entityClass} from the class of the passed in {@code rootObject}.</p>
	 * <p>Assumes that the class of {@code rootObject} is a defined class within the schema.</p>
	 * @param rootObject the root object to create the graph from
	 * @param <T> the entity class
	 * @return an object graph derived from the relationships defined in in the schema and
	 * associated with the rootObject
	 */
	public ObjectGraph execute(Object rootObject, ObjectGraph existingGraph) {
		assert(rootObject != null);
		final Class<?> entityClass = rootObject.getClass();
		assert(entityClass != null);
	
		return execute(new Query()
			.withEntityClass(entityClass)
			.withRootObjects(CollectionUtil.asSet(rootObject))
			.withExistingGraph(existingGraph));
	}

	/**
	 * Supplements an existing graph with the given data.
	 * @param existingGraph
	 * @param entityType
	 * @param firstId
	 * @param ids
	 * @return
	 */
	public <T> ObjectGraph supplement(
			ObjectGraph existingGraph, Class<?> entityType, Object firstId, Object... ids) {

		List<?> objects = getLoader(entityType).load(CollectionUtil.asList(firstId, ids));
		if (objects==null || objects.isEmpty()) {
			return existingGraph;
		}

		return execute(new Query()
				.withEntityClass(entityType)
				.withRootObjects(objects)
				.withExistingGraph(existingGraph));
	}

	/**
	 * Returns the loader for an entity class.
	 * @param entityClass the class
	 * @return the loader
	 */
	public EntityLoader<?> getLoader(Class<?> entityClass) {
		return loaders.get(entityClass);
	}

	/**
	 * Returns an execution plan for loading the given entity.
	 * @param entityClass
	 * @param ids
	 * @return
	 */
	public ExecutionPlan plan(Set<Class<?>> entityClasses) {
		ExecutionPlan ret = cachedPlans.get(entityClasses);
		if (ret!=null) {
			return ret;
		}
		ret = planner.plan(entityClasses);
		cachedPlans.put(entityClasses, ret);
		return ret;
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
