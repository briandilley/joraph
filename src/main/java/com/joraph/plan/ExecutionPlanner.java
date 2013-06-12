package com.joraph.plan;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.joraph.Context;
import com.joraph.schema.Graph;
import com.joraph.schema.Schema;

public class ExecutionPlanner {

	public static final Integer WHITE=0, GREY=1, BLACK=2;

	private Context context = null;
	private Schema schema = null;
	private ExecutionPlan plan = null;

	/**
	 * Creates a plan for the given context.
	 * @param context the context
	 */
	public ExecutionPlanner(Context context) {
		this.context = context;
	}

	/**
	 * Clears the state.
	 */
	public void clear() {
		schema = context.getSchema();
		plan = new ExecutionPlan();
	}

	/**
	 * Creates a plan for loading an object graph
	 * starting with the given {@code entityClass}.
	 * @param ctx
	 * @param entityClass
	 * @return
	 */
	public <T> ExecutionPlan plan(Class<T> entityClass) {
		clear();

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
				gatherAndLoad(buff);
				buff.clear();
			}
			buff.add(sorted.get(i));
		}

		// gather and load the remaining
		gatherAndLoad(buff);

		// return it
		return plan;
	}

	private void gatherAndLoad(Collection<Class<?>> entities) {
		for (Class<?> c : entities) {
			plan.addOperation(new GatherForeignKeysTo(c));
		}
		for (Class<?> c : entities) {
			plan.addOperation(new LoadOperation(c));
		}
	}

}
