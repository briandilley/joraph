package com.joraph.plan;

import java.util.ArrayList;
import java.util.List;

public class ParallelOperation 
	extends AbstractOperation
	implements Operation {

	private List<Operation> operations = new ArrayList<>();

	@Override
	public String explain() {
		StringBuilder ret = new StringBuilder()
			.append(" - ")
			.append(getClass().getSimpleName())
			.append(" in parallel: [\n");
		for (int i=0; i<operations.size(); i++) {
			ret.append("    ").append(operations.get(i).explain());
			if (i<operations.size()-1) {
				ret.append(",");
			}
			ret.append("\n");
		}
		return ret.append("]")
			.toString();
	}

	/**
	 * @return the operations
	 */
	public List<Operation> getOperations() {
		return operations;
	}

}
