package com.joraph;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import com.joraph.plan.ExecutionPlan;
import com.joraph.plan.ExecutionPlanner;
import com.joraph.plan.GatherForeignKeysTo;
import com.joraph.plan.LoadEntities;
import com.joraph.plan.Operation;
import com.joraph.schema.Schema;

/**
 * The object that brings everything together.
 */
public class JoraphContext {

	private Schema schema;
	private ExecutionPlanner planner;
	private Map<Class<?>, ExecutionPlan> cachedPlans;
	private Map<Class<?>, EntityLoader<?>> loaders;
	private ExecutorService executorService;

	/**
	 * Creates a context for the given {@link Schema}.
	 * @param schema
	 */
	public JoraphContext(Schema schema) {
		this.schema 		= schema;
		this.planner		= new ExecutionPlanner(this);
		this.cachedPlans	= new HashMap<>();
	}

	/**
	 * @return the schema
	 */
	public Schema getSchema() {
		return schema;
	}

	/**
	 * Returns the execution planner.
	 * @return
	 */
	public ExecutionPlanner getPlanner() {
		return planner;
	}

	public Results load(Class<?> entityClass, Collection<Serializable> ids) {

		// create plan and result
		ExecutionPlan plan = plan(entityClass);
		Results results = new Results();

		// execute the plan
		for (Operation op : plan.getOperations()) {
			executeOperation(op, results);
		}
	}

	private void executeOperation(Operation op, Results results) {

		// gather fks
		if (GatherForeignKeysTo.class.isInstance(op)) {
			gatherFksTo(results, GatherForeignKeysTo.class.cast(op).getEntityClass());

		// load
		} else if (LoadEntities.class.isInstance(op)) {
			
		}
	}

	private Collection<Serializable> gatherFksTo(Results results, Class<?> entityClass) {
		return null;
	}

	/**
	 * Returns an execution plan for loading the given entity.
	 * @param entityClass
	 * @param ids
	 * @return
	 */
	public ExecutionPlan plan(Class<?> entityClass) {
		ExecutionPlan ret = cachedPlans.get(entityClass);
		if (ret!=null) {
			return ret;
		}
		ret = planner.plan(entityClass);
		cachedPlans.put(entityClass, ret);
		return ret;
	}

}
