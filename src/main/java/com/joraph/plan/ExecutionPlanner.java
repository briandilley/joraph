package com.joraph.plan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.joraph.JoraphContext;
import com.joraph.schema.Graph;
import com.joraph.schema.Schema;

public class ExecutionPlanner {

	private JoraphContext context = null;

	/**
	 * Creates a plan for the given context.
	 * @param context the context
	 */
	public ExecutionPlanner(JoraphContext context) {
		this.context = context;
	}

	/**
	 * Creates a plan for loading an object graph
	 * starting with the given {@code entityClass}.
	 * @param ctx
	 * @param entityClass
	 * @return
	 */
	public <T> ExecutionPlan plan(Class<?> entityClass) {
		return plan(Sets.<Class<?>>newHashSet(entityClass));
	}

	/**
	 * Creates a plan for loading an object graph
	 * starting with the given {@code entityClass}.
	 * @param ctx
	 * @param entityClass
	 * @return
	 */
	public <T> ExecutionPlan plan(Set<Class<?>> entityClasses) {

		// get the schema
		Schema schema = context.getSchema();
		ExecutionPlan plan = new ExecutionPlan();

		// for each entity
		for (Class<?> entityClass : entityClasses) {

			// build the graph
			final Graph<Class<?>> graph = schema.graph(entityClass);
	
			// sort the graph
			List<Class<?>> sorted = graph.topSort();
	
			// create the plan
			Set<Class<?>> buff = new HashSet<>();
			int lastGather = 0;
			for (int i=0; i<sorted.size(); i++) {
				boolean gather = false;
				for (int j=lastGather; j<i; j++) {
					if (graph.hasOutgoingEdge(sorted.get(j), sorted.get(i))) {
						gather = true;
						lastGather = i;
						break;
					}
				}
				if (gather) {
					gatherAndLoad(plan, buff);
					buff.clear();
				}
				buff.add(sorted.get(i));
			}
	
			// gather and load the remaining
			gatherAndLoad(plan, buff);

		}

		// return it
		return plan;
	}

	@SuppressWarnings("unchecked")
	private void gatherAndLoad(ExecutionPlan plan, Collection<Class<?>> entities) {
		List<Class<?>> sortedEntities = new ArrayList<Class<?>>(entities);
		Collections.sort(List.class.cast(sortedEntities), Schema.CLASS_COMPARATOR);
		for (Class<?> c : sortedEntities) {
			plan.addOperation(new GatherForeignKeysTo(c));
		}
		for (Class<?> c : sortedEntities) {
			plan.addOperation(new LoadEntities(c));
		}
	}

}
