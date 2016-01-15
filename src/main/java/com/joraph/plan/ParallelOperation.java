package com.joraph.plan;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents steps in a query plan which may be executed in parallel.
 */
public class ParallelOperation 
	extends AbstractOperation
	implements Operation {

	private final List<Operation> operations = new ArrayList<>();

	@Override
	public String explain() {
		StringBuilder ret = new StringBuilder()
			.append(" - ")
			.append("(").append(explainCost()).append(") ").append(getClass().getSimpleName())
			.append(" in parallel: [\n");
		for (int i=0; i<operations.size(); i++) {
			ret.append("    ").append(operations.get(i).explain());
			if (i<operations.size()-1) {
				ret.append(",");
			}
			ret.append("\n");
		}
		return ret.append("   ]")
			.toString();
	}

	@Override
	public double cost() {
		return operations.stream()
				.mapToDouble(Operation::cost)
				.sum()
			+ operations.size() * 0.1d;
	}

	/**
	 * @return the operations
	 */
	public List<Operation> getOperations() {
		return operations;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((operations == null) ? 0 : operations.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParallelOperation other = (ParallelOperation) obj;
		if (operations == null) {
			if (other.operations != null)
				return false;
		} else if (!operations.equals(other.operations))
			return false;
		return true;
	}

}
