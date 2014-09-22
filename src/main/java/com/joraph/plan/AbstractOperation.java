package com.joraph.plan;

/**
 * The {@link Operation} base class.
 */
public abstract class AbstractOperation
	implements Operation {

	@Override
	public String toString() {
		return explain();
	}

}
