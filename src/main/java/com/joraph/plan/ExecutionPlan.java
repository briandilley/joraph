package com.joraph.plan;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * An execution plan.
 */
public class ExecutionPlan {

	private final List<Operation> operations = new LinkedList<>();

	public void addOperation(Operation op) {
		operations.add(op);
	}

	public List<Operation> getOperations() {
		return Collections.unmodifiableList(operations);
	}

	public String explain() {
		return this.toString();
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder()
			.append("Execution plan: \n");
		for (Operation op : operations) {
			ret.append(op.explain())
				.append("\n");
		}
		return ret.toString();
	}

}
