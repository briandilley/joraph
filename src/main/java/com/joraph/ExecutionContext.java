package com.joraph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.joraph.plan.ExecutionPlan;
import com.joraph.plan.GatherForeignKeysTo;
import com.joraph.plan.LoadEntities;
import com.joraph.plan.Operation;
import com.joraph.plan.ParallelOperation;
import com.joraph.schema.ForeignKey;
import com.joraph.schema.Key;

/**
 * An execution context which brings together a {@link com.joraph.JoraphContext},
 * a single entity class, and the root objects.
 */
public class ExecutionContext {

	private final JoraphContext context;
	private final ObjectGraph objectGraph;
	private final Class<?> entityClass;
	private final Map<Class<?>, Set<Serializable>> keysToLoad;
	private ExecutionPlan plan;

	/**
	 * Creates a new instance of ExecutionContext.
	 * @param context the {@link com.joraph.JoraphContext}
	 * @param entityClass the entity class
	 * @param rootObjects root objects to derive child objects from based on the schema
	 *                    contained within {@link com.joraph.JoraphContext}
	 * @param <T> the entity type
	 */
	public <T> ExecutionContext(JoraphContext context, Class<T> entityClass, Iterable<T> rootObjects) {
		this.context 		= context;
		this.entityClass	= entityClass;
		this.objectGraph = new ObjectGraph();
		this.keysToLoad		= new HashMap<>();

		addToResults(rootObjects, entityClass);
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

		plan = context.plan(entityClass);
		for (Operation op : plan.getOperations()) {
			executeOperation(op);
		}
		return objectGraph;
	}

	private void addToResults(Iterable<?> objects, Class<?> entityClass) {
		Key<?> pk = context.getSchema().getEntityDescriptor(entityClass).getPrimaryKey();
		for (Object obj : objects) {
			objectGraph.addResult(entityClass, pk.read(obj), obj);
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

	private void gatherForeignKeysTo(Class<?> entityClass) {
		synchronized (this.keysToLoad) {
			if (!keysToLoad.containsKey(entityClass)) {
				keysToLoad.put(entityClass, new HashSet<Serializable>());
			}
		}
		for (ForeignKey<?> fk : context.getSchema().describeForeignKeysTo(entityClass)) {
			for (Object o : objectGraph.getList(fk.getEntityClass())) {
				Serializable id = fk.read(o);
				if (objectGraph.get(entityClass, id)!=null) {
					continue;
				}
				keysToLoad.get(entityClass).add(id);
			}
		}
	}

	private void loadEntities(Class<?> entityClass) {
		if (!keysToLoad.containsKey(entityClass)
				|| keysToLoad.get(entityClass).isEmpty()) {
			return;
		}

		Set<Serializable> ids	= keysToLoad.get(entityClass);
		EntityLoader<?> loader 	= context.getLoader(entityClass);
		// TODO error handling around here, if a loader is not configured NPEs
		assert(loader != null);
		List<?> objects			= loader.load(ids);

		addToResults(objects, entityClass);

		keysToLoad.get(entityClass).clear();
	}

	private void runInParallel(List<Operation> ops) {
		throw new UnsupportedOperationException();
	}

}
