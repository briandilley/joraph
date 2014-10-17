package com.joraph;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.joraph.plan.ExecutionPlan;
import com.joraph.plan.ExecutionPlanner;
import com.joraph.schema.Schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
				.withRootObjects(Sets.newHashSet(Optional.fromNullable(objects).or(new HashSet<T>()))));
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
			.withRootObjects(Sets.newHashSet(Optional.fromNullable(objects).or(Collections.<T>emptySet())))
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
	@SuppressWarnings("unchecked")
	public <T> ObjectGraph execute(Class<T> entityClass, T rootObject) {
		return execute(new Query()
			.withEntityClass(entityClass)
			.withRootObjects(Optional.fromNullable((Set<T>)Sets.<T>newHashSet(rootObject)).or(Collections.<T>emptySet())));
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
	@SuppressWarnings("unchecked")
	public <T> ObjectGraph execute(Class<T> entityClass, T rootObject, ObjectGraph existingGraph) {
		return execute(new Query()
			.withEntityClass(entityClass)
			.withRootObjects(Optional.fromNullable((Set<T>)Sets.<T>newHashSet(rootObject)).or(Collections.<T>emptySet()))
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
	@SuppressWarnings("unchecked")
	public <T> ObjectGraph execute(T rootObject) {
		assert(rootObject != null);
		final Class<T> entityClass = (Class<T>)rootObject.getClass();
		assert(entityClass != null);
		
		return execute(new Query()
			.withEntityClass(entityClass)
			.withRootObjects(Optional.fromNullable((Set<T>)Sets.<T>newHashSet(rootObject)).or(Collections.<T>emptySet())));
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
	@SuppressWarnings("unchecked")
	public <T> ObjectGraph execute(T rootObject, ObjectGraph existingGraph) {
		assert(rootObject != null);
		final Class<T> entityClass = (Class<T>)rootObject.getClass();
		assert(entityClass != null);
	
		return execute(new Query()
			.withEntityClass(entityClass)
			.withRootObjects(Optional.fromNullable((Set<T>)Sets.<T>newHashSet(rootObject)).or(Collections.<T>emptySet()))
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

		Iterable<?> objects = getLoader(entityType).load(Lists.asList(firstId, ids));
		if (Iterables.isEmpty(objects)) {
			return existingGraph;
		}

		return execute(new Query()
				.withEntityClass(entityType)
				.withRootObjects(Sets.newHashSet(objects))
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
