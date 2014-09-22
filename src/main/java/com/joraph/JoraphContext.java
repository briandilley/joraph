package com.joraph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.joraph.plan.ExecutionPlan;
import com.joraph.plan.ExecutionPlanner;
import com.joraph.schema.Schema;

/**
 * Derives a series of {@link com.joraph.plan.Operation} from a
 * {@link com.joraph.schema.Schema}.
 */
public class JoraphContext {

	private final Schema schema;
	private final ExecutionPlanner planner;
	private final Map<Class<?>, ExecutionPlan> cachedPlans;
	private final Map<Class<?>, EntityLoader<?>> loaders;

	/**
	 * Creates a context for the given {@link Schema}.
	 * @param schema
	 */
	public JoraphContext(Schema schema) {
		this.schema 		= schema;
		this.planner		= new ExecutionPlanner(this);
		this.cachedPlans	= new HashMap<>();
		this.loaders		= new HashMap<>();
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
	 * @param entityClass
	 * @param ids
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> Results executeForIds(Class<T> entityClass, Iterable<Serializable> ids) {
		List<?> objs = loaders.get(entityClass).load(ids);
		return execute(entityClass, (List<T>)objs);
	}

	/**
	 * 
	 * @param entityClass
	 * @param ids
	 * @return
	 */
	public <T> Results execute(Class<T> entityClass, Iterable<T> objs) {
		return new ExecutionContext(this, entityClass, objs).execute();
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
	public ExecutionPlan plan(Class<?> entityClass) {
		ExecutionPlan ret = cachedPlans.get(entityClass);
		if (ret!=null) {
			return ret;
		}
		ret = planner.plan(entityClass);
		cachedPlans.put(entityClass, ret);
		return ret;
	}

	/**
	 * @return the schema
	 */
	public Schema getSchema() {
		return schema;
	}

}
