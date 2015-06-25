package com.joraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Sets;
import com.joraph.debug.JoraphDebug;
import com.joraph.plan.ExecutionPlan;
import com.joraph.plan.GatherForeignKeysTo;
import com.joraph.plan.LoadEntities;
import com.joraph.plan.Operation;
import com.joraph.plan.ParallelOperation;
import com.joraph.schema.EntityDescriptor;
import com.joraph.schema.ForeignKey;
import com.joraph.schema.Property;
import com.joraph.schema.Schema;

/**
 * An execution context which brings together a {@link com.joraph.JoraphContext},
 * a single entity class, and the root objects.
 */
public class ExecutionContext {

	private final JoraphContext context;
	private final Query query;
	private final Map<Class<?>, Set<Object>> keysToLoad;
	private final ObjectGraph objectGraph;
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
		this.keysToLoad		= new HashMap<>();

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

		for (Class<?> entityClass : entityClasses) {
			final EntityDescriptor entityDescriptor = schema.getEntityDescriptor(entityClass);
			if (entityDescriptor == null) {
				throw new UnknownEntityDescriptorException(entityClass);
			}
	
			Property<?> pk = entityDescriptor.getPrimaryKey();
			assert(pk != null);
			for (Object obj : objects) {
				if (obj.getClass().equals(entityClass)) {
					objectGraph.addResult(entityClass, pk.read(obj), obj);
				}
			}
		}
	}

	private void executeOperation(Operation op) {
		if (GatherForeignKeysTo.class.isInstance(op)) {
			gatherForeignKeysTo(GatherForeignKeysTo.class.cast(op).getEntityClass());

		} else if (LoadEntities.class.isInstance(op)) {
			loadEntities(LoadEntities.class.cast(op).getEntityClass());

		} else if (ParallelOperation.class.isInstance(op)) {
			runInParallel(ParallelOperation.class.cast(op).getOperations());
		}
	}

	@SuppressWarnings("unchecked")
	private void gatherForeignKeysTo(Class<?> entityClass) {
		synchronized (this.keysToLoad) {
			if (!keysToLoad.containsKey(entityClass)) {
				keysToLoad.put(entityClass, new HashSet<Object>());
			}
		}
		for (ForeignKey<?> fk : context.getSchema().describeForeignKeysTo(entityClass)) {
			for (Object o : objectGraph.getList(fk.getEntityClass())) {
				Object val = fk.read(o);
				if (val==null) {
					continue;
				}
				Set<Object> ids;
				if (Collection.class.isInstance(val)) {
					ids = Sets.newHashSet((Collection<Object>)val);
				} else if (val.getClass().isArray()) {
					ids = Sets.newHashSet((Object[])val);
				} else {
					ids = Sets.newHashSet(val);
				}
				for (Object id : ids) {
					if (id==null || objectGraph.get(entityClass, id)!=null) {
						continue;
					}
					keysToLoad.get(entityClass).add(id);
				}
			}
		}
	}

	private void loadEntities(Class<?> entityClass) {
		Set<Object> ids = keysToLoad.get(entityClass);
		if (ids == null || ids.isEmpty()) {
			return;
		}
		final EntityLoader<?> loader = context.getLoader(entityClass);
		if (loader == null) {
			throw new UnconfiguredLoaderException(entityClass);
		}

		List<?> objects = loader.load(ids);
		addToResults(objects, Sets.<Class<?>>newHashSet(entityClass));

		keysToLoad.get(entityClass).clear();
	}

	private void runInParallel(List<Operation> ops) {
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (final Operation op : ops) {
			futures.add(context.getExecutorService().submit(new Runnable() {
				@Override
				public void run() {
					executeOperation(op);
				}
			}));
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

}
