package com.joraph.plan;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * An execution plan.
 */
public class ExecutionPlan
		implements Operation {

	private final List<Operation> operations = new LinkedList<>();
	private final Set<Class<?>> entityClasses;

	public ExecutionPlan(Set<Class<?>> entityClasses) {
		this.entityClasses = new HashSet<>(entityClasses);
	}

	public void addOperation(Operation op) {
		operations.add(op);
	}

	public List<Operation> getOperations() {
		return Collections.unmodifiableList(operations);
	}

	public String explain() {
		StringBuilder ret = new StringBuilder()
				.append("(").append(explainCost()).append(") ").append("Execution plan: \n");
			for (Class<?> entityClass : entityClasses) {
				ret.append(" * ")
					.append(entityClass.getName())
					.append("\n");
			}
			for (int i=0; i<operations.size(); i++) {
				ret.append(operations.get(i).explain())
					.append("\n");
			}
			return ret.toString();
	}

	public double cost() {
		return operations.stream()
				.mapToDouble(Operation::cost)
				.sum();
	}

	@Override
	public String toString() {
		return this.explain();
	}

}
