package com.joraph.plan;

public abstract class AbstractOperation
	implements Operation {

	@Override
	public String toString() {
		return explain();
	}

}
