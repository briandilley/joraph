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

public class ExecutionContext {

	private ExecutionPlan plan;
	private JoraphContext context;
	private Results results;
	private Class<?> entityClass;
	private Map<Class<?>, Set<Serializable>> keysToLoad;

	public <T> ExecutionContext(JoraphContext context, Class<T> entityClass, Iterable<T> objects) {
		this.context 		= context;
		this.entityClass	= entityClass;
		this.results 		= new Results();
		this.keysToLoad		= new HashMap<>();

		addToResults(objects, entityClass);
	}

	public Results execute() {
		if (plan!=null) {
			throw new IllegalStateException("Already run");
		}
		plan = context.plan(entityClass);
		for (Operation op : plan.getOperations()) {
			executeOperation(op);
		}
		return results;
	}

	private void addToResults(Iterable<?> objects, Class<?> entityClass) {
		Key<?> pk = context.getSchema().getEntityDescriptor(entityClass).getPrimaryKey();
		for (Object obj : objects) {
			results.addResult(entityClass, pk.read(obj), obj);
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
			for (Object o : results.getList(fk.getEntityClass())) {
				Serializable id = fk.read(o);
				if (results.get(entityClass, id)!=null) {
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
		List<?> objects			= loader.load(ids);

		addToResults(objects, entityClass);

		keysToLoad.get(entityClass).clear();
	}

	private void runInParallel(List<Operation> ops) {
		throw new UnsupportedOperationException();
	}

}