package com.joraph;

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
import com.joraph.schema.EntityDescriptor;
import com.joraph.schema.ForeignKey;
import com.joraph.schema.Key;
import com.joraph.schema.Schema;
import com.sun.org.apache.bcel.internal.classfile.Unknown;

/**
 * An execution context which brings together a {@link com.joraph.JoraphContext},
 * a single entity class, and the root objects.
 */
public class ExecutionContext {

	private final JoraphContext context;
	private final ObjectGraph objectGraph;
	private final Class<?> entityClass;
	private final Map<Class<?>, Set<Object>> keysToLoad;
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
		if (objects == null) {
			return;
		}

		final Schema schema = context.getSchema();
		assert(schema != null);
		final EntityDescriptor entityDescriptor = schema.getEntityDescriptor(entityClass);
		if (entityDescriptor == null) {
			throw new UnknownEntityDescriptorException(entityClass);
		}

		Key<?> pk = entityDescriptor.getPrimaryKey();
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
				keysToLoad.put(entityClass, new HashSet<Object>());
			}
		}
		for (ForeignKey<?> fk : context.getSchema().describeForeignKeysTo(entityClass)) {
			for (Object o : objectGraph.getList(fk.getEntityClass())) {
				Object id = fk.read(o);
				if (id==null || objectGraph.get(entityClass, id)!=null) {
					continue;
				}
				keysToLoad.get(entityClass).add(id);
			}
		}
	}

	private void loadEntities(Class<?> entityClass) {
		final EntityLoader<?> loader = context.getLoader(entityClass);
		if (loader == null) {
			throw new UnconfiguredLoaderException(entityClass);
		}
		final Set<Object> ids = keysToLoad.get(entityClass);
		if (ids == null || ids.isEmpty()) {
			return;
		}

		List<?> objects = loader.load(ids);
		addToResults(objects, entityClass);

		keysToLoad.get(entityClass).clear();
	}

	private void runInParallel(List<Operation> ops) {
		throw new UnsupportedOperationException();
	}

}
