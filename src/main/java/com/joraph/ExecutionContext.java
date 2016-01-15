package com.joraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.joraph.debug.DebugInfo;
import com.joraph.debug.JoraphDebug;
import com.joraph.loader.EntityLoader;
import com.joraph.loader.UnconfiguredLoaderException;
import com.joraph.plan.ExecutionPlan;
import com.joraph.plan.GatherForeignKeysTo;
import com.joraph.plan.LoadEntities;
import com.joraph.plan.Operation;
import com.joraph.plan.ParallelOperation;
import com.joraph.schema.EntityDescriptor;
import com.joraph.schema.Property;
import com.joraph.schema.Schema;
import com.joraph.schema.UnknownEntityDescriptorException;

/**
 * An execution context which brings together a {@link com.joraph.JoraphContext},
 * a single entity class, and the root objects.
 */
public class ExecutionContext {

	private final JoraphContext context;
	private final Query query;
	private final ObjectGraph objectGraph;
	private final KeysToLoad keysToLoad;
	private ExecutionPlan plan;

	/**
	 * Creates a new instance of ExecutionContext.
	 * @param context the {@link com.joraph.JoraphContext}
	 * @param entityClass the entity class
	 * @param rootObjects root objects to derive child objects from based on the schema
	 *                    contained within {@link com.joraph.JoraphContext}
	 * @param existingGraph an existing graph to populate
	 */
	public ExecutionContext(JoraphContext context, Query query) {
		this.context		= context;
		this.query 			= query;
		this.keysToLoad		= new KeysToLoad();

		this.objectGraph = query.hasExistingGraph()
				? query.getExistingGraph()
				: new ObjectGraph(context.getSchema());
		
		addToResults(query.getRootObjects(), query.getEntityClasses());
	}

	/**
	 * <p>Executes the plan, iterates the resulting operations, and returns the results.</p>
	 * <p>Subsequent calls to {@code execute} result in a cached {@link ObjectGraph}.</p>
	 * @return the results derived from loading the associated objects supplied in the root
	 * objects
	 */
	public ObjectGraph execute() {
		if (plan!=null) {
			return objectGraph;
		}

		keysToLoad.clear();

		plan = context.plan(query.getEntityClasses());
		for (Operation op : plan.getOperations()) {
			executeOperation(op);
		}

		JoraphDebug.addExecutionPlan(plan);
		JoraphDebug.addObjectGraph(objectGraph);

		return objectGraph;
	}

	private void addToResults(Iterable<?> objects, Set<Class<?>> entityClasses) {
		if (objects == null) {
			return;
		}

		final Schema schema = context.getSchema();
		assert(schema != null);

		for (Object object : objects) {
			if (object==null) {
				continue;
			}
			final EntityDescriptor<?> entityDescriptor = schema.getEntityDescriptors(object.getClass())
					.findFirstByEntityClass(object.getClass())
					.orElseThrow(() -> new UnknownEntityDescriptorException(object.getClass()));

			final Property<?, ?> pk = entityDescriptor.getPrimaryKey();
			objectGraph.addResult(entityDescriptor.getGraphKey(), pk.read(object), object);
		}
	}

	private void executeOperation(Operation op) {
		if (GatherForeignKeysTo.class.isInstance(op)) {
			gatherValuesForForeignKeysTo(GatherForeignKeysTo.class.cast(op).getEntityClass());

		} else if (LoadEntities.class.isInstance(op)) {
			loadEntities(LoadEntities.class.cast(op).getEntityClass());

		} else if (ParallelOperation.class.isInstance(op)) {
			runInParallel(ParallelOperation.class.cast(op).getOperations());
		}
	}

	private void gatherValuesForForeignKeysTo(Class<?> entityClass) {

		context.getSchema().getEntityDescriptors(entityClass).stream()
				.flatMap((entityDescriptor) -> context.getSchema().describeForeignKeysTo(entityClass).stream()
						.flatMap((fk) -> objectGraph.stream(fk.getEntityClass())
								.filter((o) -> o.getClass().equals(fk.getEntityClass()))
								.map(fk::read)
								.filter(Objects::nonNull)
								.map(CollectionUtil::convertToSet)
								.flatMap(Set::stream)
								.filter(Objects::nonNull)
								.filter(objectGraph.hasFunction(entityDescriptor.getEntityClass()).negate())))
				.forEach(keysToLoad.getAddKeyFunction(entityClass));

	}

	private void loadEntities(Class<?> entityClass) {
		Set<Object> ids = keysToLoad.getKeys(entityClass);
		if (ids == null || ids.isEmpty()) {
			return;
		}
		final EntityLoader<?> loader = context.getLoader(entityClass);
		if (loader == null) {
			throw new UnconfiguredLoaderException(entityClass);
		}

		long start = System.currentTimeMillis();

		List<?> objects;

		try {
			objects = loader.load(ids);
		} catch(Throwable t) {
			throw new JoraphException(
					"Error invoking loader: "+loader.toString()
					+" with ids: "+String.join(", ", ids.stream()
							.map(Object::toString)
							.limit(5)
							.collect(Collectors.toList()))
					+((ids.size() > 5)
							? "... and "+(ids.size()-5)+" more"
							: ""),
				t);
		}

		JoraphDebug.addLoaderDebug(
				entityClass,
				System.currentTimeMillis()-start,
				ids, objects);

		addToResults(objects, CollectionUtil.asSet(entityClass));

		keysToLoad.removeKeys(entityClass, ids);
	}

	private void runInParallel(List<Operation> ops) {
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (final Operation op : ops) {
			final DebugInfo info = JoraphDebug.getDebugInfo();
			futures.add(context.getExecutorService().submit(new Runnable() {
				@Override
				public void run() {
					JoraphDebug.setThreadDebugInfo(info);
					executeOperation(op);
					JoraphDebug.clearThreadDebugInfo();
				}
			}));
			JoraphDebug.setThreadDebugInfo(info);
		}
		for (Future<?> future : futures) {
			try {
				future.get(context.getParallelExecutorDefaultTimeoutMillis(), TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				throw new JoraphException(e);
			}
		}
	}

	/**
	 * @return the context
	 */
	public JoraphContext getContext() {
		return context;
	}

	/**
	 * @return the query
	 */
	public Query getQuery() {
		return query;
	}

	/**
	 * @return the objectGraph
	 */
	public ObjectGraph getObjectGraph() {
		return objectGraph;
	}

	/**
	 * @return the plan
	 */
	public ExecutionPlan getPlan() {
		return plan;
	}

	/**
	 * Simple class for managing the keys that need to be loaded.
	 */
	private static class KeysToLoad {
		private final Map<Class<?>, Set<Object>> map = new ConcurrentHashMap<>();
		private Consumer<Object> getAddKeyFunction(Class<?> entityClass) {
			return (id) -> addKey(entityClass, id);
		}
		private void addKey(Class<?> entityClass, Object id) {
			entitySet(entityClass).add(id);
		}
		private void removeKeys(Class<?> entityClass, Collection<Object> ids) {
			entitySet(entityClass).removeAll(ids);
		}
		private Set<Object> getKeys(Class<?> entityClass) {
			return entitySet(entityClass);
		}
		private Set<Object> entitySet(Class<?> entityClass) {
			return map.computeIfAbsent(entityClass, __ ->
					Collections.newSetFromMap(new ConcurrentHashMap<>()));
		}
		private void clear() {
			map.clear();
		}
	}

}
