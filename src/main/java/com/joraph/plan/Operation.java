package com.joraph.plan;

public interface Operation {

	/**
	 * Explains the operation in human readable form.
	 * @return the plan
	 */
	String explain();

	/**
	 * Returns the estimated cost of the {@link Operation}.
	 * @return the estimated cost
	 */
	default double cost() {
		return 1f;
	}

	default String explainCost() {
		return String.format("%.3f", cost());
	}

}
