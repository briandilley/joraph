package com.joraph.plan;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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
		Graph graph = schema.graph(entityClass);

		// get entities with no incoming FKs
		Stack<Class<?>> stack = new Stack<>();
		stack.addAll(graph.getEntitiesWithoutIncomingEdges());

		// loop
		List<Class<?>> order = new ArrayList<>();
		while (!stack.isEmpty()) {
			Class<?> n = stack.pop();
			order.add(n);
			
			for (Class<?> m : graph.getEntities()) {
				if (!graph.hasEdge(n, m)) {
					continue;
				}
				graph.removeEdge(n, m);
				if (!graph.hasIncomingEdge(m)) {
					stack.push(m);
				}
			}
		}

		return plan;
	}

}
