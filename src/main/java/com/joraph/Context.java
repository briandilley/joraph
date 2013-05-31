package com.joraph;

import java.util.Collection;

import com.joraph.plan.ExecutionPlan;
import com.joraph.schema.Schema;

public class Context {

	private Schema schema;

	public Context(Schema schema) {
		this.schema = schema;
	}

	/**
	 * @return the schema
	 */
	public Schema getSchema() {
		return schema;
	}

	public ExecutionPlan explain(Class<?> entityClass, Collection<?> ids) {
		return null;
	}

}
